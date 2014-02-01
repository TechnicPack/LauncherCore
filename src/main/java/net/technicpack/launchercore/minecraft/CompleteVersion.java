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

package net.technicpack.launchercore.minecraft;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CompleteVersion implements Version {

	private String id;
	private Date time;
	private Date releaseTime;
	private ReleaseType type;
	private String minecraftArguments;
	private List<Library> libraries;
	private String mainClass;
	private int minimumLauncherVersion;
	private String incompatibilityReason;
	private List<Rule> rules;
	private String assets;
	private transient boolean areAssetsVirtual;

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public ReleaseType getType() {
		return this.type;
	}

	@Override
	public void setType(ReleaseType type) {
		this.type = type;
	}

	@Override
	public Date getUpdatedTime() {
		return this.time;
	}

	@Override
	public void setUpdatedTime(Date updatedTime) {
		this.time = updatedTime;
	}

	@Override
	public Date getReleaseTime() {
		return this.releaseTime;
	}

	@Override
	public void setReleaseTime(Date releaseTime) {
		this.releaseTime = releaseTime;
	}

	public String getMinecraftArguments() {
		return this.minecraftArguments;
	}

	public List<Library> getLibraries() {
		return this.libraries;
	}

	public List<Library> getLibrariesForOS() {
		List<Library> libraryList = new ArrayList<>(this.libraries.size());
		for (Library library : this.libraries) {
			if (library.isForCurrentOS()) {
				libraryList.add(library);
			}
		}
		return libraryList;
	}

	public String getMainClass() {
		return this.mainClass;
	}

	public int getMinimumLauncherVersion() {
		return this.minimumLauncherVersion;
	}

	public String getIncompatibilityReason() {
		return this.incompatibilityReason;
	}

	public List<Rule> getRules() {
		return this.rules;
	}

	public String getAssetsKey() {
		return this.assets;
	}

	public boolean getAreAssetsVirtual() {
		return this.areAssetsVirtual;
	}

	public void setAreAssetsVirtual(boolean areAssetsVirtual) {
		this.areAssetsVirtual = areAssetsVirtual;
	}

	@Override
	public String toString() {
		return "CompleteVersion{" +
				"id='" + this.id + '\'' +
				", time=" + this.time +
				", releaseTime=" + this.releaseTime +
				", type=" + this.type +
				", minecraftArguments='" + this.minecraftArguments + '\'' +
				", libraries=" + this.libraries +
				", mainClass='" + this.mainClass + '\'' +
				", minimumLauncherVersion=" + this.minimumLauncherVersion +
				", incompatibilityReason='" + this.incompatibilityReason + '\'' +
				", rules=" + this.rules +
				'}';
	}
}
