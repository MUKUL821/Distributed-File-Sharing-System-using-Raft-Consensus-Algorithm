package gash.router.client;

import io.netty.channel.Channel;
import routing.Pipe.CommandMessage;

/**
 * queues outgoing messages - this provides surge protection if the client
 * creates large numbers of messages.
 * 
 * @author gash
 * 
 */
public class CommWorker extends Thread {
	private CommConnection conn;
	private boolean forever = true;

	public CommWorker(CommConnection conn) {
		this.conn = conn;

		if (conn.outbound == null)
			throw new RuntimeException("connection worker detected null queue");
	}

	@Override
	public void run() {
		System.out.println("--> starting worker thread");
		System.out.flush();

		Channel ch = conn.connect();
		if (ch == null || !ch.isOpen() || !ch.isActive()) {
			CommConnection.logger.error("connection missing, no outbound communication");
			return;
		}

		while (true) {
			if (!forever && conn.outbound.size() == 0)
				break;

			try {
				// block until a message is enqueued AND the outgoing
				// channel is active
				//conn.outbound.size();
				CommandMessage msg = conn.outbound.take();
				//System.out.println("Filename ifrom q" +msg.getRequest().getFile().getFilename());
				//System.out.println("chk here"+ch.isWritable());
				if (ch.isWritable()) {System.out.println("in if"+msg.getRequest().getFile().getData().size());
					if (!conn.write(msg)) {
						conn.outbound.putFirst(msg);
					}

					System.out.flush();
				} else {
					//System.out.println("--> channel not writable- tossing out msg!");

				 conn.outbound.putFirst(msg);
				}

				System.out.flush();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
				break;
			} catch (Exception e) {
				CommConnection.logger.error("Unexpected communcation failure", e);
				break;
			}
		}

		if (!forever) {
			CommConnection.logger.info("connection queue closing");
		}
	}
}
