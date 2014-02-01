package net.technicpack.launchercore.exception;

import java.io.IOException;

public class BuildInaccessibleException extends IOException {

	private static final long serialVersionUID = -4905270588640056830L;
	private String packDisplayName;
	private String build;
	private Throwable cause;

	public BuildInaccessibleException(String displayName, String build) {
		this.packDisplayName = displayName;
		this.build = build;
	}

	public BuildInaccessibleException(String displayName, String build, Throwable cause) {
		this(displayName, build);
		this.cause = cause;
	}

	@Override
	public String getMessage() {
		if (this.cause != null) {
			Throwable rootCause = this.cause;

			while (rootCause.getCause() != null) {
				rootCause = rootCause.getCause();
			}

			return "An error was raised while attempting to read pack info for modpack "+this.packDisplayName+", build "+this.build+": "+rootCause.getMessage();
		}
		return "The pack host returned unrecognizable garbage while attempting to read pack info for modpack "+this.packDisplayName+", build "+this.build+".";
	}

	@Override
	public synchronized Throwable getCause() {
		return this.cause;
	}
}
