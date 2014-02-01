/*
 * This file is part of Technic Launcher Core.
 * Copyright (C) 2013 Syndicate, LLC
 *
 * Technic Launcher Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Technic Launcher Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License,
 * as well as a copy of the GNU Lesser General Public License,
 * along with Technic Launcher Core.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.technicpack.launchercore.install;

import net.technicpack.launchercore.restful.PackInfo;
import net.technicpack.launchercore.restful.Resource;
import net.technicpack.launchercore.util.Download;
import net.technicpack.launchercore.util.DownloadUtils;
import net.technicpack.launchercore.util.MD5Utils;
import net.technicpack.launchercore.util.ResourceUtils;
import net.technicpack.launchercore.util.Utils;

import org.apache.commons.io.FileUtils;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class InstalledPack {
	public static final String RECOMMENDED = "recommended";
	public static final String LATEST = "latest";
	public static final String LAUNCHER_DIR = "launcher\\";
	public static final String MODPACKS_DIR = "%MODPACKS%\\";

	private static BufferedImage BACKUP_LOGO;
	private static BufferedImage BACKUP_BACKGROUND;
	private static BufferedImage BACKUP_ICON;
	private transient AtomicReference<BufferedImage> logo = new AtomicReference<>();
	private transient AtomicReference<BufferedImage> background = new AtomicReference<>();
	private transient AtomicReference<BufferedImage> icon = new AtomicReference<>();
	private transient HashMap<AtomicReference<BufferedImage>, AtomicReference<Boolean>> downloading = new HashMap<>(3);
	private transient File installedDirectory;
	private transient File binDir;
	private transient File configDir;
	private transient File savesDir;
	private transient File cacheDir;
	private transient File resourceDir;
	private transient File modsDir;
	private transient File coremodsDir;
	private transient PackInfo info;
	private transient PackRefreshListener refreshListener;
	private String name;
	private boolean platform;
	private String build;
	private String directory;

	private transient boolean isLocalOnly;

	public InstalledPack(String name, boolean platform, String build, String directory) {
		this();
		this.name = name;
		this.platform = platform;
		this.build = build;
		this.directory = directory;
	}

	public InstalledPack(String name, boolean platform) {
		this(name, platform, RECOMMENDED, MODPACKS_DIR + name);
	}

	@SuppressWarnings("boxing")
	public InstalledPack() {
		this.downloading.put(this.logo, new AtomicReference<>(false));
		this.downloading.put(this.background, new AtomicReference<>(false));
		this.downloading.put(this.icon, new AtomicReference<>(false));
		this.isLocalOnly = false;
		this.build = RECOMMENDED;
	}

	public void setRefreshListener(PackRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
	}

	public String getDirectory() {
		String path = this.directory;
		if (this.directory != null && this.directory.startsWith(LAUNCHER_DIR)) {
			path = new File(Utils.getLauncherDirectory(), this.directory.substring(9)).getAbsolutePath();
		}
		if (this.directory != null && this.directory.startsWith(MODPACKS_DIR)) {
			path = new File(Utils.getModpacksDirectory(), this.directory.substring(11)).getAbsolutePath();
		}
		return path;
	}

	public void initDirectories() {
		this.binDir = new File(this.installedDirectory, "bin");
		this.configDir = new File(this.installedDirectory, "config");
		this.savesDir = new File(this.installedDirectory, "saves");
		this.cacheDir = new File(this.installedDirectory, "cache");
		this.resourceDir = new File(this.installedDirectory, "resources");
		this.modsDir = new File(this.installedDirectory, "mods");
		this.coremodsDir = new File(this.installedDirectory, "coremods");

		this.binDir.mkdirs();
		this.configDir.mkdirs();
		this.savesDir.mkdirs();
		this.cacheDir.mkdirs();
		this.resourceDir.mkdirs();
		this.modsDir.mkdirs();
		this.coremodsDir.mkdirs();
	}

	public String getDisplayName() {
		if (this.info == null) {
			return this.name;
		}
		return this.info.getDisplayName();
	}

	public boolean isPlatform() {
		return this.platform;
	}

	public PackInfo getInfo() {
		return this.info;
	}

	public void setInfo(PackInfo info) {
		this.info = info;
		this.isLocalOnly = false;
	}

	public boolean isLocalOnly() {
		return this.isLocalOnly;
	}

	public void setLocalOnly() {
		this.isLocalOnly = true;
	}

	public boolean hasLogo()
	{
		return (getLogo() != BACKUP_LOGO);
	}

	public String getBuild() {
		if (this.info != null)
		{
			if (this.build.equals(RECOMMENDED)) {
				return this.info.getRecommended();
			}
			if (this.build.equals(LATEST)) {
				return this.info.getLatest();
			}
		}

		return this.build;
	}

	public void setBuild(String build) {
		this.build = build;
	}

	public String getRawBuild() {
		return this.build;
	}

	public File getInstalledDirectory() {
		if (this.installedDirectory == null) {
			setPackDirectory(new File(getDirectory()));
		}
		return this.installedDirectory;
	}

	public void setPackDirectory(File packPath) {
		if (this.installedDirectory != null) {
			try {
				FileUtils.copyDirectory(this.installedDirectory, packPath);
				FileUtils.cleanDirectory(this.installedDirectory);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		this.installedDirectory = packPath;
		String path = this.installedDirectory.getAbsolutePath();
		if (path.startsWith(Utils.getModpacksDirectory().getAbsolutePath())) {
			this.directory = MODPACKS_DIR + path.substring(Utils.getModpacksDirectory().getAbsolutePath().length() + 1);
		} else if (path.startsWith(Utils.getLauncherDirectory().getAbsolutePath())) {
			this.directory = LAUNCHER_DIR + path.substring(Utils.getLauncherDirectory().getAbsolutePath().length() + 1);
		}
		initDirectories();
	}

	public File getBinDir() {
		return this.binDir;
	}

	public File getConfigDir() {
		return this.configDir;
	}

	public File getSavesDir() {
		return this.savesDir;
	}

	public File getCacheDir() {
		return this.cacheDir;
	}

	public File getResourceDir() {
		return this.resourceDir;
	}

	public File getModsDir() {
		return this.modsDir;
	}

	public File getCoremodsDir() {
		return this.coremodsDir;
	}

	public synchronized BufferedImage getLogo() {
		if (this.logo.get() != null) {
			return this.logo.get();
		}
		Resource resource = this.info != null ? this.info.getLogo() : null;
		if (loadImage(this.logo, "logo.png", resource)) {
			return this.logo.get();
		}

		if (BACKUP_LOGO == null) {
			BACKUP_LOGO = loadBackup("/org/spoutcraft/launcher/resources/noLogo.png");
		}
		return BACKUP_LOGO;
	}

	private boolean loadImage(AtomicReference<BufferedImage> image, @SuppressWarnings("hiding") String name, Resource resource) {
		File assets = new File(Utils.getAssetsDirectory(), "packs");
		File packs = new File(assets, getName());
		packs.mkdirs();
		File resourceFile = new File(packs, name);

		String url = "";
		String md5 = "";

		if (resource != null) {
			url = resource.getUrl();
			md5 = resource.getMd5();
		}

		boolean cached = loadCachedImage(image, resourceFile, url, md5);

		if (!cached) {
			downloadImage(image, resourceFile, url, md5);
		}

		if (image.get() == null) {
			return false;
		}

		return cached;
	}

	private static boolean loadCachedImage(AtomicReference<BufferedImage> image, File file, String url, String md5) {
		try {
			if (file.exists() && (url.isEmpty() || md5.isEmpty() || MD5Utils.getMD5(file).equalsIgnoreCase(md5))) {
				BufferedImage newImage;
				newImage = ImageIO.read(file);
				image.set(newImage);
				return true;
			}
		} catch (IIOException e) {
			Utils.getLogger().log(Level.INFO, "Failed to load image " + file.getAbsolutePath() + " from file.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	@SuppressWarnings("boxing")
	private void downloadImage(final AtomicReference<BufferedImage> image, final File temp, final String url, final String md5) {
		if (url.isEmpty() || this.downloading.get(image).get()) {
			return;
		}

		this.downloading.get(image).set(true);
		@SuppressWarnings("hiding")
		final String name = getName();
		final InstalledPack pack = this;
		Thread thread = new Thread(name + " Image Download Worker") {
			@SuppressWarnings("synthetic-access")
			@Override
			public void run() {
				try {
					if (temp.exists()) {
						System.out.println("Pack: " + getName() + " Calculated MD5: " + MD5Utils.getMD5(temp) + " Required MD5: " + md5);
					}
					Download download = DownloadUtils.downloadFile(url, temp.getName(), temp.getAbsolutePath());
					BufferedImage newImage;
					newImage = ImageIO.read(download.getOutFile());
					image.set(newImage);
					InstalledPack.this.downloading.get(image).set(false);
					if (InstalledPack.this.refreshListener != null) {
						InstalledPack.this.refreshListener.refreshPack(pack);
					}
				} catch (IOException e) {
					System.out.println("Failed to download and load image from: " + url);
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}

	public String getName() {
		return this.name;
	}

	private static BufferedImage loadBackup(String backup) {
		try {
			return ImageIO.read(ResourceUtils.getResourceAsStream(backup));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public synchronized BufferedImage getBackground() {
		if (this.background.get() != null) {
			return this.background.get();
		}
		Resource resource = this.info != null ? this.info.getBackground() : null;
		if (loadImage(this.background, "background.jpg", resource)) {
			return this.background.get();
		}

		if (BACKUP_BACKGROUND == null) {
			BACKUP_BACKGROUND = loadBackup("/org/spoutcraft/launcher/resources/background.jpg");
		}
		return BACKUP_BACKGROUND;
	}

	public synchronized BufferedImage getIcon() {
		if (this.icon.get() != null) {
			return this.icon.get();
		}
		Resource resource = this.info != null ? this.info.getIcon() : null;
		if (loadImage(this.icon, "icon.png", resource)) {
			return this.icon.get();
		}

		if (BACKUP_ICON == null) {
			BACKUP_ICON = loadBackup("/org/spoutcraft/launcher/resources/icon.png");
		}
		return BACKUP_ICON;
	}

	public String getIconPath() {
		return Utils.getAssetsDirectory() + "/packs/" + getName() + "/icon.png";
	}

	@Override
	public String toString() {
		return "InstalledPack{" +
				"info=" + this.info +
				", name='" + this.name + '\'' +
				", platform=" + this.platform +
				", build='" + this.build + '\'' +
				", directory='" + this.directory + '\'' +
				'}';
	}
}
