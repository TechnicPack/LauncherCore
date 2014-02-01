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

package net.technicpack.launchercore.restful.platform;

import net.technicpack.launchercore.exception.BuildInaccessibleException;
import net.technicpack.launchercore.exception.RestfulAPIException;
import net.technicpack.launchercore.install.user.User;
import net.technicpack.launchercore.restful.Modpack;
import net.technicpack.launchercore.restful.PackInfo;
import net.technicpack.launchercore.restful.PlatformConstants;
import net.technicpack.launchercore.restful.Resource;
import net.technicpack.launchercore.restful.RestObject;

import java.util.ArrayList;
import java.util.List;

public class PlatformPackInfo extends RestObject implements PackInfo {
	private String name;
	private String displayName;
	private String url;
	private Resource icon;
	private Resource logo;
	private Resource background;
	private String minecraft;
	private String forge;
	private String version;
	private String solder;
	private boolean forceDir;

	public PlatformPackInfo() {

	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public String getUrl() {
		return this.url;
	}

	@Override
	public Resource getIcon() {
		return this.icon;
	}

	@Override
	public Resource getBackground() {
		return this.background;
	}

	@Override
	public Resource getLogo() {
		return this.logo;
	}

	@Override
	public String getRecommended() {
		return this.version;
	}

	@Override
	public String getLatest() {
		return this.version;
	}

	@Override
	public boolean shouldForceDirectory() {
		return this.forceDir;
	}

	@Override
	public List<String> getBuilds() {
		List<String> builds = new ArrayList<>();
		builds.add(this.version);
		return builds;
	}

	public String getMinecraft() {
		return this.minecraft;
	}

	public String getForge() {
		return this.forge;
	}

	public String getSolder() {
		return this.solder;
	}

	public boolean hasSolder() {
		return this.solder != null && !this.solder.equals("");
	}

	@Override
	public Modpack getModpack(String build, User user) throws BuildInaccessibleException {
		return new Modpack(this);
	}

	@Override
	public String toString() {
		return "PlatformPackInfo{" +
				"name='" + this.name + '\'' +
				", displayName='" + this.displayName + '\'' +
				", url='" + this.url + '\'' +
				", icon=" + this.icon +
				", logo=" + this.logo +
				", background=" + this.background +
				", minecraft='" + this.minecraft + '\'' +
				", forge='" + this.forge + '\'' +
				", version='" + this.version + '\'' +
				", solder='" + this.solder + '\'' +
				", forceDir=" + this.forceDir +
				'}';
	}

	public static PlatformPackInfo getPlatformPackInfo(String name) throws RestfulAPIException {
		return getRestObject(PlatformPackInfo.class, PlatformConstants.getPlatformInfoUrl(name));
	}
}
