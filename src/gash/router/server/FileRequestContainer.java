package gash.router.server;

import java.util.concurrent.LinkedBlockingDeque;

public class FileRequestContainer {
	public static volatile LinkedBlockingDeque<FileRequestObject> readRequestQueue = new LinkedBlockingDeque<>();
}