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

import net.technicpack.launchercore.util.OperatingSystem;
import net.technicpack.launchercore.util.Utils;
import org.apache.commons.lang3.text.StrSubstitutor;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Library {
	private static final StrSubstitutor SUBSTITUTOR = new StrSubstitutor();
	private static final String[] fallback = {"http://mirror.technicpack.net/Technic/lib/", "http://search.maven.org/remotecontent?filepath="};
	private String name;
	private List<Rule> rules;
	private Map<OperatingSystem, String> natives;
	private ExtractRules extract;
	private String url;

	public String getName() {
		return this.name;
	}

	public List<Rule> getRules() {
		return this.rules;
	}

	public Map<OperatingSystem, String> getNatives() {
		return this.natives;
	}

	public ExtractRules getExtract() {
		return this.extract;
	}

	public boolean isForCurrentOS() {
		if (this.rules == null) {
			return true;
		}

		Rule.Action lastAction = Rule.Action.DISALLOW;

		for (Rule rule : this.rules) {
			Rule.Action action = rule.getAction();
			if (action != null) {
				lastAction = action;
			}
		}

		return lastAction.equals(Rule.Action.ALLOW);
	}

	public String getArtifactPath() {
		return getArtifactPath(null);
	}

	public String getArtifactPath(String classifier) {
		String baseDir = getArtifactBaseDir();
		if (this.name == null ) {
			throw new IllegalStateException("Cannot get artifact path of empty/blank artifact");
		}
		if(baseDir == null || baseDir.equals("")){
			throw new IllegalStateException("No base artifact path");
		}
		return String.format("%s/%s", getArtifactBaseDir(), getArtifactFilename(classifier));
	}

	public String getArtifactBaseDir() {
		if (this.name == null) {
			throw new IllegalStateException("Cannot get artifact dir of empty/blank artifact");
		}
		try {
			String[] parts = this.name.split(":", 3);
			return String.format("%s/%s/%s", parts[0].replaceAll("\\.", "/"), parts[1], parts[2]);
		}
		catch (Exception exc){
			exc.printStackTrace();
			Utils.getLogger().log(Level.SEVERE, "Failure to return artifact base dir. Returning null.");
			return null;
		}
	}

	public String getArtifactFilename(String classifier) {
		if (this.name == null) {
			throw new IllegalStateException("Cannot get artifact filename of empty/blank artifact");
		}

		String[] parts = this.name.split(":", 3);
		String result;

		if (classifier != null) {
			result = String.format("%s-%s%s.jar", parts[1], parts[2], "-" + classifier);
		} else {
			result = String.format("%s-%s%s.jar", parts[1], parts[2], "");
		}

		return SUBSTITUTOR.replace(result);
	}

	public String getDownloadUrl(String path) {
		if (this.url != null) {
			String checkUrl = this.url + path;
			if (Utils.pingHttpURL(checkUrl)) {
				return checkUrl;
			}
			for (String string : fallback) {
				checkUrl = string + path;
				if (Utils.pingHttpURL(checkUrl)) {
					return checkUrl;
				}
			}
		}
		return " https://libraries.minecraft.net/" + path;
	}
}
