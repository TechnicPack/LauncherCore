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

package net.technicpack.launchercore.util;

import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.technicpack.launchercore.exception.DownloadException;
import net.technicpack.launchercore.exception.PermissionDeniedException;

import org.apache.commons.io.IOUtils;

public class Download implements Runnable {
	private static final long TIMEOUT = 30000;

	private URL url;
	private long size = -1;
	private long downloaded = 0;
	private String outPath;
	private String name;
	private DownloadListener listener;
	private Result result = Result.FAILURE;
	private File outFile = null;
	private Exception exception = null;

	public Download(String url, String name, String outPath) throws MalformedURLException {
		this.url = new URL(url);
		this.outPath = outPath;
		this.name = name;
	}

	public float getProgress() {
		return ((float) this.downloaded / this.size) * 100;
	}

	public Exception getException() {
		return this.exception;
	}

	@Override
	@SuppressWarnings("unused")
	public void run() {
		@SuppressWarnings("resource")
		ReadableByteChannel rbc = null;
		@SuppressWarnings("resource")
		FileOutputStream fos = null;
		try {
			HttpURLConnection conn = Utils.openHttpConnection(this.url);
			int response = conn.getResponseCode();
			int responseFamily = response/100;

			if (responseFamily == 3) {
				throw new DownloadException("The server issued a redirect response which Technic failed to follow.");
			} else if (responseFamily != 2) {
				throw new DownloadException("The server issued a "+response+" response code.");
			}

			@SuppressWarnings("resource")
			InputStream in = getConnectionInputStream(conn);

			this.size = conn.getContentLength();
			this.outFile = new File(this.outPath);
			this.outFile.delete();

			rbc = Channels.newChannel(in);
			fos = new FileOutputStream(this.outFile);

			stateChanged();

			Thread progress = new MonitorThread(Thread.currentThread(), rbc);
			progress.start();

			fos.getChannel().transferFrom(rbc, 0, this.size > 0 ? this.size : Integer.MAX_VALUE);
			in.close();
			rbc.close();
			progress.interrupt();
			if (this.size > 0) {
				if (this.size == this.outFile.length()) {
					this.result = Result.SUCCESS;
				}
			} else {
				this.result = Result.SUCCESS;
			}
		} catch (PermissionDeniedException e) {
			this.exception = e;
			this.result = Result.PERMISSION_DENIED;
		} catch (DownloadException e) {
			this.exception = e;
			this.result = Result.FAILURE;
		} catch (Exception e) {
			this.exception = e;
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(rbc);
		}
	}

	protected InputStream getConnectionInputStream(final URLConnection urlconnection) throws DownloadException {
		final AtomicReference<InputStream> is = new AtomicReference<InputStream>();

		for (int j = 0; (j < 3) && (is.get() == null); j++) {
			StreamThread stream = new StreamThread(urlconnection, is);
			stream.start();
			int iterationCount = 0;
			while ((is.get() == null) && (iterationCount++ < 5)) {
				try {
					stream.join(1000L);
				} catch (InterruptedException ignore) {
				}
			}

			if (stream.permDenied.get()) {
				throw new PermissionDeniedException("Permission denied!");
			}

			if (is.get() != null) {
				break;
			}
			try {
				stream.interrupt();
				stream.join();
			} catch (InterruptedException ignore) {
			}
		}

		if (is.get() == null) {
			throw new DownloadException("Unable to download file from " + urlconnection.getURL());
		}
		return new BufferedInputStream(is.get());
	}

	private void stateChanged() {
		if (this.listener != null)
			this.listener.stateChanged(this.name, getProgress());
	}

	public void setListener(DownloadListener listener) {
		this.listener = listener;
	}

	public Result getResult() {
		return this.result;
	}

	public File getOutFile() {
		return this.outFile;
	}

	private static class StreamThread extends Thread {
		private final URLConnection urlconnection;
		private final AtomicReference<InputStream> is;
		public final AtomicBoolean permDenied = new AtomicBoolean(false);

		public StreamThread(URLConnection urlconnection, AtomicReference<InputStream> is) {
			this.urlconnection = urlconnection;
			this.is = is;
		}

		@Override
		public void run() {
			try {
				this.is.set(this.urlconnection.getInputStream());
			} catch (SocketException e) {
				if (e.getMessage().equalsIgnoreCase("Permission denied: connect")) {
					this.permDenied.set(true);
				}
			} catch (IOException ignore) {
			}
		}
	}

	private class MonitorThread extends Thread {
		private final ReadableByteChannel rbc;
		private final Thread downloadThread;
		private long last = System.currentTimeMillis();

		public MonitorThread(Thread downloadThread, ReadableByteChannel rbc) {
			super("Download Monitor Thread");
			this.setDaemon(true);
			this.rbc = rbc;
			this.downloadThread = downloadThread;
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void run() {
			while (!this.isInterrupted()) {
				long diff = Download.this.outFile.length() - Download.this.downloaded;
				Download.this.downloaded = Download.this.outFile.length();
				if (diff == 0) {
					if ((System.currentTimeMillis() - this.last) > TIMEOUT) {
						if (Download.this.listener != null) {
							Download.this.listener.stateChanged("Download Failed", getProgress());
						}
						try {
							this.rbc.close();
							this.downloadThread.interrupt();
						} catch (Exception ignore) {
							//We catch all exceptions here, because ReadableByteChannel is AWESOME
							//and was throwing NPE's sometimes when we tried to close it after
							//the connection broke.
						}
						return;
					}
				} else {
					this.last = System.currentTimeMillis();
				}

				stateChanged();
				try {
					sleep(50);
				} catch (InterruptedException ignore) {
					return;
				}
			}
		}
	}

	public enum Result {
		SUCCESS, FAILURE, PERMISSION_DENIED,
	}
}
