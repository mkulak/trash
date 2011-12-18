package com.xap4o.flash

import scala.collection.JavaConversions._
import java.net.InetSocketAddress
import org.apache.commons.logging.{Log, LogFactory}
import java.nio.ByteBuffer
import java.io.IOException
import java.nio.channels._

class FlashSecurityServer extends Thread {
  private val LOG:Log = LogFactory.getLog(classOf[FlashSecurityServer])
  private val RESPONSE_BYTES = "<cross-domain-policy><allow-access-from domain='*' to-ports='*' /></cross-domain-policy>\r\n".getBytes("UTF-8")
  private val selector:Selector = Selector.open()

  setDaemon(true)
  setName("flash security server thread")
  
  override def run {
    val ssc = ServerSocketChannel.open();
    ssc.socket().bind(new InetSocketAddress(843));
    ssc.configureBlocking(false);
    ssc.register(selector, SelectionKey.OP_ACCEPT);
    LOG.info("flash security server started")
    loop
  }

  def loop {
    while (true) {
      process
      Thread.sleep(40)
    }
  }

  def process {
    if (selector.select > 0) {
      val keys = selector.selectedKeys
      keys.foreach((key:SelectionKey) => {
        if (key.isAcceptable) {
          handleAccept(key)
        } else if (key.isWritable) {
          handleWrite(key)
        }
      })
      keys.clear
    }
  }

  def handleAccept(key: SelectionKey) {
    val channel = key.channel.asInstanceOf[ServerSocketChannel].accept
    channel.configureBlocking(false)
    val buf = ByteBuffer.allocate(RESPONSE_BYTES.length)
    buf.put(RESPONSE_BYTES)
    channel.register(selector, SelectionKey.OP_WRITE, buf)
  }

  def handleWrite(key: SelectionKey) {
    val ch = key.channel.asInstanceOf[SocketChannel]
    val buf = key.attachment.asInstanceOf[ByteBuffer]
    if (buf.position > 0) {
      buf.flip
      try {
        ch.write(buf)
      }
      catch {
        case e: IOException => {
          closeQuietly(ch)
          key.cancel
        }
      }
      buf.compact
    } else {
      LOG.info("policy request handled")
      closeQuietly(ch)
      key.cancel
    }
  }

  def closeQuietly(c:Channel) {
    try {
      c.close
    }
    catch {
      case ignore:IOException => ()
    }
  }
}
