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

package net.technicpack.launchercore.launch;

import java.util.List;

public class LaunchOptions {
	private int width;
	private int height;
	private boolean fullscreen;
	private String title;
	private String iconPath;

	public LaunchOptions(String title, String iconPath, int width, int height, boolean fullscreen) {
		this.width = width;
		this.height = height;
		this.fullscreen = fullscreen;
		this.title = title;
		this.iconPath = iconPath;
	}

	public String getTitle() {
		return this.title;
	}

	public String getIconPath() {
		return this.iconPath;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public boolean getFullscreen() {
		return this.fullscreen;
	}

	public void appendToCommands(List<String> commands) {
		if (getTitle() != null) {
			commands.add("--title");
			commands.add(this.title);
		}

		if (getWidth() > -1) {
			commands.add("--width");
			commands.add(Integer.toString(getWidth()));
		}

		if (getHeight() > -1) {
			commands.add("--height");
			commands.add(Integer.toString(getHeight()));
		}

		if (getFullscreen()) {
			commands.add("--fullscreen");
		}

		if (getIconPath() != null) {
			commands.add("--icon");
			commands.add(getIconPath());
		}
		
		/*
		 * I want to make this all optional, but couldnt find where the config is loaded in either the core or launcher
		 * So I couldnt make a enable-beta-args.
		 * I wanted to do that so you could test it without accepting the pull first.
		 * Or is there a way to do that?
		 *		
		 * Most of this is GC stuff, but GC is a big deal.
		 * 
		 * I wrote some of these and found others.
		 * 
		 * On average, people get 30 more FPS with these (on low functioning clients)
		 */
		
		// Makes the VM use agressive (the most effective) optimizations and try harder to do them
		// HUGELY speeds up minecraft
		commands.add("-XX:+AgressiveOpts");
		
		// Makes minecraft utilize all of the cores
		commands.add("-XX:ParallelGCThreads=" + Runtime.getRuntime().availableProcessors());
	
		// Changes the passes it takes for an item to be moved to old
		// This value seems to work well for MC
		commands.add("-XX:MaxTenuringThreshold=15");
		
		// Another garbage collector related param
		commands.add("-XX:+UseBiasedLocking");
		
		// 2 other forms of GC to use, seems to help
		commands.add("-XX:+UseConcMarkSweepGC");
		commands.add("-XX:+UseParNewGC");
	}
}
