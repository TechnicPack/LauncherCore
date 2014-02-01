package net.technicpack.launchercore.install;

import net.technicpack.launchercore.exception.RestfulAPIException;
import net.technicpack.launchercore.install.user.IAuthListener;
import net.technicpack.launchercore.install.user.User;
import net.technicpack.launchercore.restful.PackInfo;
import net.technicpack.launchercore.restful.RestObject;
import net.technicpack.launchercore.restful.platform.PlatformPackInfo;
import net.technicpack.launchercore.restful.solder.FullModpacks;
import net.technicpack.launchercore.restful.solder.Solder;
import net.technicpack.launchercore.restful.solder.SolderConstants;
import net.technicpack.launchercore.restful.solder.SolderPackInfo;
import net.technicpack.launchercore.util.Utils;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class AvailablePackList implements IAuthListener, PackRefreshListener {
	IPackStore mPackStore;
	Collection<String> mForcedSolderPacks = new ArrayList<>();
	private List<IPackListener> mPackListeners = new LinkedList<>();

	public AvailablePackList(IPackStore packStore) {
		this.mPackStore = packStore;
		this.mPackStore.put(new AddPack());
	}

	public void addForcedSolderPack(String solderLocation) {
		this.mForcedSolderPacks.add(solderLocation);
	}

	@Override
	public void userChanged(User user) {
		if (user == null) {
			return;
		}

		reloadAllPacks(user);
	}

	public void addPackListener(IPackListener listener) {
		this.mPackListeners.add(listener);
	}

	public void removePackListener(IPackListener listener) {
		this.mPackListeners.remove(listener);
	}

	public void triggerUpdateListeners(InstalledPack pack) {
		for (IPackListener listener : this.mPackListeners) {
			listener.updatePack(pack);
		}
	}

	@Override
	public void refreshPack(InstalledPack pack) {
		final InstalledPack threadPack = pack;
		final AvailablePackList packList = this;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				packList.triggerUpdateListeners(threadPack);
			}
		});
	}

	public void add(InstalledPack pack) {
		this.mPackStore.add(pack);
		this.mPackStore.save();
		pack.setRefreshListener(this);
		triggerUpdateListeners(pack);
	}

	public void put(InstalledPack pack) {
		this.mPackStore.put(pack);
		this.mPackStore.save();
		pack.setRefreshListener(this);
		triggerUpdateListeners(pack);
	}

	public void remove(InstalledPack pack) {
		this.mPackStore.remove(pack.getName());
		this.mPackStore.save();
	}

	public InstalledPack getOffsetPack(int offset) {
		int index = this.mPackStore.getSelectedIndex();

		index += offset;

		while (index < 0) { index += this.mPackStore.getInstalledPacks().size(); }
		while (index >= this.mPackStore.getInstalledPacks().size()) { index -= this.mPackStore.getInstalledPacks().size(); }

		return this.mPackStore.getInstalledPacks().get(this.mPackStore.getPackNames().get(index));
	}

	public void setPack(InstalledPack pack) {

		int index = this.mPackStore.getPackNames().indexOf(pack.getName());

		if (index >= 0)
		{
			this.mPackStore.setSelectedIndex(index);
			this.mPackStore.save();
		}
	}

	public void save() {
		this.mPackStore.save();
	}

	public void reloadAllPacks(User user) {
		final User threadUser = user;
		final AvailablePackList packList = this;

		for (final String packName : this.mPackStore.getPackNames()) {
			final InstalledPack pack = this.mPackStore.getInstalledPacks().get(packName);
			if (pack.isPlatform()) {
				Thread thread = new Thread(pack.getName() + " Info Loading Thread") {
					@Override
					public void run() {
						try {
							String name = pack.getName();
							PlatformPackInfo platformPackInfo = PlatformPackInfo.getPlatformPackInfo(name);
							PackInfo info = platformPackInfo;
							if (platformPackInfo.hasSolder()) {
								SolderPackInfo solderPackInfo = SolderPackInfo.getSolderPackInfo(platformPackInfo.getSolder(), name, threadUser);
								info = solderPackInfo;
							}

							info.getLogo();
							info.getIcon();
							info.getBackground();
							pack.setInfo(info);
							pack.setRefreshListener(packList);
						} catch (RestfulAPIException e) {
							Utils.getLogger().log(Level.WARNING, "Unable to load platform pack " + pack.getName(), e);
							pack.setLocalOnly();
							pack.setRefreshListener(packList);
						}

						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								packList.triggerUpdateListeners(pack);
							}
						});
					}
				};

				thread.start();
			}
		}

		Thread thread = new Thread("Technic Solder Defaults") {
			@Override
			public void run() {
				int index = 0;

				try {
					FullModpacks technic = RestObject.getRestObject(FullModpacks.class, SolderConstants.getFullSolderUrl(SolderConstants.TECHNIC, threadUser.getProfile().getName()));
					Solder solder = new Solder(SolderConstants.TECHNIC, technic.getMirrorUrl());
					for (SolderPackInfo info : technic.getModpacks().values()) {
						String name = info.getName();
						info.setSolder(solder);

						InstalledPack pack = null;
						if (AvailablePackList.this.mPackStore.getInstalledPacks().containsKey(name)) {
							pack = AvailablePackList.this.mPackStore.getInstalledPacks().get(info.getName());
							pack.setRefreshListener(packList);
							pack.setInfo(info);
						} else {
							pack = new InstalledPack(name, false);
							pack.setRefreshListener(packList);
							pack.setInfo(info);
							AvailablePackList.this.mPackStore.add(pack);
						}

						final InstalledPack deferredPack = pack;

						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								packList.triggerUpdateListeners(deferredPack);
							}
						});

						AvailablePackList.this.mPackStore.reorder(index, name);
						index++;
					}
				} catch (RestfulAPIException e) {
					Utils.getLogger().log(Level.WARNING, "Unable to load technic modpacks", e);

					for (String packName : AvailablePackList.this.mPackStore.getPackNames())
					{
						InstalledPack pack = AvailablePackList.this.mPackStore.getInstalledPacks().get(packName);
						if (!pack.isPlatform() && pack.getInfo() == null && pack.getName() != null)
							pack.setLocalOnly();
					}
				}
			}
		};
		thread.start();

		thread = new Thread("Forced Solder Thread") {

			@Override
			public void run() {
				for (String solder : AvailablePackList.this.mForcedSolderPacks) {
					try {
						SolderPackInfo info = SolderPackInfo.getSolderPackInfo(solder, threadUser);
						if (info == null) {
							throw new RestfulAPIException();
						}

						info.getLogo();
						info.getIcon();
						info.getBackground();

						InstalledPack pack = null;
						if (AvailablePackList.this.mPackStore.getInstalledPacks().containsKey(info.getName())) {
							pack = AvailablePackList.this.mPackStore.getInstalledPacks().get(info.getName());
							pack.setInfo(info);
						} else {
							pack = new InstalledPack(info.getName(), true);
							pack.setRefreshListener(packList);
							pack.setInfo(info);
							AvailablePackList.this.mPackStore.add(pack);
						}

						final InstalledPack deferredPack = pack;

						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								packList.triggerUpdateListeners(deferredPack);
							}
						});
					} catch (RestfulAPIException e) {
						Utils.getLogger().log(Level.WARNING, "Unable to load forced solder pack " + solder, e);
					}
				}
			}
		};

		thread.start();
	}
}
