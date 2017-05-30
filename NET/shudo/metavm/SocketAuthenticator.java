/*
  This file is part of shuJIT,
  Just In Time compiler for Sun Java Virtual Machine.

  Copyright (C) 1996,1997,1998,2001 Kazuyuki Shudo

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

  $Id$
*/

package NET.shudo.metavm;


import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.StringTokenizer;


/**
 * identd $B$J$I$N(B Authentication Server (RFC931) $B$+$i(B
 * OS$BL>!"%f!<%6(BID $B$rF@$k%/%i%9!#(B<BR>
 * UDP $B$G$O$J$/(B TCP $B$rMxMQ!#(B
 */
public class SocketAuthenticator {
  private static final String COMMAND = "portident";

  private static final int AUTH_PORT = 113;
	// should get with getservbyname(3)


  private String responce;
  private String OSName;
  private String userID;
  private boolean error = false;


  public SocketAuthenticator(Socket sock) throws IOException {
    this(sock.getInetAddress(), sock.getLocalPort(), sock.getPort());
  }

  public SocketAuthenticator(String host, int localPort, int remotePort)
	throws IOException /* includes UnknownHostException */ {
    this(InetAddress.getByName(host), localPort, remotePort);
  }

  /**
   * Auth. Server $B$+$i(B OS$BL>!"%f!<%6(BID $B$rF@$k!#(B<BR>
   * $B<:GT$7$?>l9g$O(B IOException $B$r(B throw $B$9$k!#(B<BR>
   * $B;XDj$5$l$?%]!<%H$rMxMQ$7$F$$$k%f!<%6$,$$$J$+$C$?>l9g$O(B
   * SocketAuthenticator#isNoUser() $B$,??$rJV$9$h$&$K$J$k!#(B
   *
   * @see isNoUser()
   */
  public SocketAuthenticator(InetAddress addr, int localPort, int remotePort)
	throws IOException {
    // Auth. Server $B$K@\B3(B
    Socket authSock;
    authSock = new Socket(addr, AUTH_PORT);	// throws IOException

    // session
    {
      DataOutputStream dout = new DataOutputStream(authSock.getOutputStream());
      BufferedReader din = new BufferedReader(
		new InputStreamReader(authSock.getInputStream(), "ASCII"));

      String request = remotePort + ", " + localPort + "\r\n";
//System.out.print("req: " + request);
      dout.writeBytes(request);
      this.responce = din.readLine();
//System.out.println("res: " + responce);
    }	// throws IOException

    // $B2r@O(B
    StringTokenizer tokenizer = new StringTokenizer(
	responce.substring(responce.indexOf(':') + 1), " :\t\n\r");
    int numToken = tokenizer.countTokens();
    if (numToken < 2)
      throw new IOException("Illegal responce");

    String[] args = new String[numToken];
    int index = 0;
    while (tokenizer.hasMoreElements()) {
      args[index++] = (String)tokenizer.nextElement();
    }
//for (int i=0; i < args.length; i++)  System.out.print("[" + args[i] + "]");
//System.out.println();

    String resType = args[0].toLowerCase();
    if (resType.equals("userid")) {
//System.out.println("numToken: " + numToken);
//System.out.println("error: " + error);
      if (numToken < 3)
	throw new IOException("Illegal responce");
      OSName = args[1];
      userID = args[2];
    }
    else if (resType.equals("error")) {
      error = true;  return;
    }
    else
      throw new IOException("Illegal responce");
  }


  /**
   * Auth. Server $B$N%l%s%9%]%s%9$rJV$9!#(B
   */
  public String responce() { return responce; }

  /**
   * OS$BL>(B $B$rJV$9!#(B
   */
  public String OSName() { return OSName; }

  /**
   * $B%f!<%6(BID $B$rJV$9!#(B
   */
  public String userID() { return userID; }

  /**
   * $B%3%s%9%H%i%/%?$G;XDj$7$?%]!<%H$rMxMQ$7$F$$$k%f!<%6$,$$$J$1$l$P??!#(B
   */
  public boolean isNoUser() { return error; }


  private static void usage(String cmd) {
    System.out.print("usage: ");
    System.out.print(cmd);
    System.out.println(" hostname localport remoteport");
  }


  public static void main(String[] args) throws IOException {
    int localPort, remotePort;

    if (args.length < 3) {
      usage(COMMAND);  System.exit(1);
    }

    localPort = Integer.parseInt(args[1]);
    remotePort = Integer.parseInt(args[2]);

    SocketAuthenticator id =
	new SocketAuthenticator(args[0], localPort, remotePort);

    if (id.isNoUser()) {
      System.out.println("Error occurred.");
    }
    else {
      System.out.println("responce: " +id.responce());
      System.out.println("  OS  : " + id.OSName());
      System.out.println("  user: " + id.userID());
    }
  }
}
