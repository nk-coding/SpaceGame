package com.nkcoding.communication;

import com.nkcoding.communication.transmissions.IntTransmission;
import com.nkcoding.communication.transmissions.PeerInfoTransmission;
import com.nkcoding.communication.transmissions.TransmissionTransmission;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class SocketCommunication extends Communication {

    private final ConcurrentLinkedQueue<Transmission> receivedTransmissions;

    private final ConcurrentMap<Integer, Connection> connections;

    private CopyOnWriteArraySet<Integer> peerSet;

    private ServerSocket serverSocket;

    private Thread serverAcceptingThread;

    /**
     * the id for this SocketCommunication
     * is -1 if it is not set yet
     * is 0 if this is the server
     */
    private int id = -1;

    /**
     * the next id for a client
     * is increased, if it is used
     * has only effect if isServer
     */
    private int idCounter = 1;

    /**
     * create a new communication instance
     *
     * @param isServer should it be the server?
     * @param port     the port to use
     */
    public SocketCommunication(boolean isServer, int port) {
        super(isServer, port);
        receivedTransmissions = new ConcurrentLinkedQueue<>();
        connections = new ConcurrentHashMap<>();
        peerSet = new CopyOnWriteArraySet<>();
        if (isServer) {
            setID(0);
        }
        try {
            //create the ServerSocket with  a thread for it and start it
            serverSocket = new ServerSocket(port);
            serverAcceptingThread = new Thread(() -> {
                while (!serverSocket.isClosed()) {
                    try {
                        Socket socket = serverSocket.accept();
                        System.out.println("accepted socket");
                        System.out.println(socket.getRemoteSocketAddress().toString());
                        //if this is a server, then a new id is generated
                        //otherwise the id must be known by the peer already
                        //also start the tread
                        if (isServer()) {
                            Connection connection = new Connection(socket, idCounter);
                            connection.start();
                            idCounter++;
                            //tell the connection who he is
                            connection.send(new IntTransmission(Transmission.SET_ID, connection.peerID));
                            List<PeerInfoTransmission.PeerInfo> peerInfos = new ArrayList<>();
                            for (Connection other : connections.values()) {
                                if (other != connection && other.remotePortAvailable()) {
                                    peerInfos.add(new PeerInfoTransmission.PeerInfo(other.getRemoteIP(), other.remotePort, other.peerID));
                                }
                            }
                            connection.send(new PeerInfoTransmission(Transmission.ADD_ID, peerInfos.toArray(PeerInfoTransmission.PeerInfo[]::new)));
                        } else {
                            new Connection(socket).start();
                        }
                    } catch (IOException e) {
                        System.err.println("errow while opening socket");
                        e.printStackTrace();
                    }
                }
            });
            serverAcceptingThread.start();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("could not create ServerSocket", e);
        }
    }

    public static int getEphemeralPort() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("no port available");
        }
    }

    /**
     * start the communication with a peer
     * This peer MUST NOT be the server to use this method
     *
     * @param ip the ip address of the peer
     */
    @Override
    public void openCommunication(String ip, int port) {
        //check that this is not a server, to prevent serious errors
        if (isServer()) {
            throw new IllegalStateException("must not be the server");
        } else {
            Connection connection = openConnection(ip, port, 0);
            if (connection != null && id == -1) {
                System.out.println("try to get own id");
                connection.send(new Transmission(Transmission.GET_PEER_ID));
            }
        }
    }

    /**
     * send data to a specified peer with a specific id
     *
     * @param peer         the id got from openCommunication
     * @param transmission the transmission to send
     */
    @Override
    public void sendTo(int peer, Transmission transmission) {
        Connection connection = connections.get(peer);
        if (connection == null) {
            throw new IllegalArgumentException("peer does not exist");
        } else {
            connection.send(transmission);
        }
    }

    /**
     * sends some data to all peers
     *
     * @param transmission the transmission to send
     */
    @Override
    public void sendToAll(Transmission transmission) {
        for (Connection connection : connections.values()) {
            connection.send(transmission);
        }
    }

    /**
     * checks if there are any received transmissions
     *
     * @return true if there are any transmissions to receive
     */
    @Override
    public boolean hasTransmissions() {
        return !receivedTransmissions.isEmpty();
    }

    /**
     * gets the oldest received transmission, if it was not internal
     *
     * @return the Transmission or null if none was available
     */
    @Override
    public Transmission getTransmission() {
        return receivedTransmissions.poll();
    }

    /**
     * get a list of all peers
     *
     * @return a list with all peers
     */
    @Override
    public Set<Integer> getPeers() {
        return Collections.unmodifiableSet(peerSet);
    }

    /**
     * get the id of this peer
     *
     * @return the id
     */
    @Override
    public int getId() {
        return this.id;
    }

    /**
     * closes all sockets and tries to stop al threads
     */
    @Override
    public void close() {
        serverAcceptingThread.interrupt();
        try {
            serverAcceptingThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Connection connection : connections.values()) {
            connection.interrupt();
            try {
                connection.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connection.close();
        }
    }

    private void setID(int newID) {
        this.id = newID;
    }

    /**
     * internal version of openConnection
     * used to ad new peers on other clients
     *
     * @param ip the new peer ip
     * @param id the id for the new peer
     */
    private void addPeer(String ip, int port, int id) {
        Connection connection = openConnection(ip, port, id);
    }


    /**
     * internally used to open a connection
     *
     * @param ip the address for remote
     * @return the Connection
     */
    private Connection openConnection(String ip, int port, int peerID) {
        try {
            System.out.println(ip + ", " + port);
            Socket peerSocket = new Socket();
            peerSocket.connect(new InetSocketAddress(ip, port), 7000);
            Connection connection = new Connection(peerSocket, port, peerID);
            connection.start();
            return connection;
        } catch (IOException e) {
            System.err.println("could not open socket");
            e.printStackTrace();
            try {
                //send information to the other peer, that a redirect is necessary
                connections.get(0).send(new TransmissionTransmission(Transmission.REDIRECT_TRANSMISSION, 0, peerID,
                        new IntTransmission(Transmission.ADD_ID_REDIRECTION, this.id)));
                return new Connection(null, port, peerID);
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new IllegalArgumentException("cannot open port");
            }
        }
    }

    /**
     * used to shutdown a specific connection
     *
     * @param idToShutdown
     */
    private void shutdownConnection(int idToShutdown) {
        Connection toShutdown = connections.remove(idToShutdown);
        if (toShutdown != null) {
            System.out.println("shutdown " + idToShutdown);
            toShutdown.shutdown = true;
            toShutdown.interrupt();
            toShutdown.close();
        }
        peerSet.remove(idToShutdown);
    }


    private class Connection extends Thread implements Closeable {
        public Socket socket;
        public ObjectInputStream inputStream = null;
        public ObjectOutputStream outputStream = null;
        public int remotePort = -1;
        public volatile boolean shutdown = false;
        private int peerID = -1;

        public Connection(Socket socket, int remotePort, int peerID) throws IOException {
            setPeerID(peerID);
            this.socket = socket;
            this.remotePort = remotePort;
            if (socket != null) {
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.flush();
                inputStream = new ObjectInputStream(socket.getInputStream());
            }
            if (peerID == -1) {
                //try to get the id
                System.out.println("send peerID request");
                send(new Transmission(Transmission.GET_ID));
            }
            if (remotePort == -1) {
                //find out remote port
                System.out.println("request remote port");
                send(new Transmission(Transmission.GET_PORT));
            }
        }

        /**
         * wrapper for Connection(socket, -1, peerID)
         */
        public Connection(Socket socket, int peerID) throws IOException {
            this(socket, -1, peerID);
        }

        /**
         * wrapper for Connection(socket, -1, -1)
         */
        public Connection(Socket socket) throws IOException {
            this(socket, -1, -1);
        }

        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    Transmission transmission = (Transmission) inputStream.readObject();
                    handleTransmission(transmission);
                } catch (IOException e) {
                    System.err.println("IOException occurred while reading on " + peerID);
                    e.printStackTrace();
                    break;
                } catch (Exception e) {
                    System.err.println("probably different project versions " + peerID);
                    e.printStackTrace();
                    break;
                }
            }
            if (!shutdown) {
                if (isServer()) {
                    //shutdown and send to all clients
                    System.out.println("shutdown from server");
                    shutdownConnection(peerID);
                    sendToAll(new IntTransmission(Transmission.REMOVE_CONNECTION, peerID));
                } else {
                    //this is not a planned shutdown, so go to server mode and request remove request
                    System.out.println("connection from " + id + " to " + peerID + " failed, go to server mode");
                    try {
                        socket.close();
                    } catch (Exception e) {
                        System.out.println("exception while closing socket");
                    }
                    this.socket = null;
                    sendTo(0, new IntTransmission(Transmission.REMOVE_CONNECTION_REQUEST, peerID));
                }
            }
        }

        void handleTransmission(Transmission transmission) {
            switch (transmission.getId()) {
                case Transmission.SET_PEER_ID:
                    //change the id
                    int newID = ((IntTransmission) transmission).value;
                    setPeerID(newID);
                    break;
                case Transmission.SET_ID:
                    int nID = ((IntTransmission) transmission).value;
                    if (SocketCommunication.this.id != nID) {
                        setID(nID);
                    }
                    break;
                case Transmission.GET_ID:
                    //if the id is requested, the peer wants to know my id, so a
                    //SET_PEER_ID is sent back
                    System.out.println("(requested) I AM " + SocketCommunication.this.id);
                    send(new IntTransmission(Transmission.SET_PEER_ID, SocketCommunication.this.id));
                    break;
                case Transmission.GET_PEER_ID:
                    //if the peer id is requested, the peer wants to know its own id, so
                    //a SET_ID is sent back
                    if (this.peerID == -1) {
                        System.err.println("tries to get unknown id: " + peerID);
                    } else {
                        System.out.println("send peer id: " + peerID);
                        send(new IntTransmission(Transmission.SET_ID, this.peerID));
                    }
                    break;
                case Transmission.ADD_ID:
                    //add a new peer
                    PeerInfoTransmission pit = (PeerInfoTransmission) transmission;
                    for (PeerInfoTransmission.PeerInfo peerInfo : pit.peerInfos) {
                        addPeer(peerInfo.ip, peerInfo.port, peerInfo.peerID);
                    }
                    break;
                case Transmission.GET_PORT:
                    send(new IntTransmission(Transmission.SET_PORT, SocketCommunication.this.port));
                    break;
                case Transmission.SET_PORT:
                    this.remotePort = ((IntTransmission) transmission).value;
                    break;
                case Transmission.REDIRECT_TRANSMISSION:
                    TransmissionTransmission transmissionTransmission = (TransmissionTransmission) transmission;
                    if (transmissionTransmission.to == id) {
                        //this is the target
                        Connection connection = connections.get(transmissionTransmission.from);
                        if (connection != null) {
                            connection.handleTransmission(transmissionTransmission.transmission);
                        } else {
                            System.out.println("cannot handle transmission");
                        }
                    } else {
                        Connection connection = connections.get(transmissionTransmission.to);
                        if (connection != null) {
                            connection.send(transmission);
                        } else {
                            System.out.println("cannot redirect transmission: " + transmissionTransmission.to);
                        }
                    }
                    break;
                case Transmission.ADD_ID_REDIRECTION:
                    try {
                        new Connection(null, ((IntTransmission) transmission).value);
                    } catch (IOException e) {
                        System.err.println("IOException while opening redirect connection");
                        e.printStackTrace();
                    }
                    break;
                case Transmission.REMOVE_CONNECTION:
                    int idToShutdown = ((IntTransmission) transmission).value;
                    shutdownConnection(idToShutdown);
                    break;
                case Transmission.REMOVE_CONNECTION_REQUEST:
                    if (isServer()) {
                        //TODO maybe improve this with a ping and a timeout, but this should be good enough for this
                        int removeRequest = ((IntTransmission) transmission).value;
                        if (!connections.containsKey(removeRequest)) {
                            sendToAll(new IntTransmission(Transmission.REMOVE_CONNECTION, removeRequest));
                        } else {
                            System.out.println("not necessary to remove connection to " + removeRequest);
                        }
                    } else {
                        System.err.println("this can only be done on a server");
                    }
                    break;
                default:
                    //redirect to the other transmissions
                    receivedTransmissions.add(transmission);
            }
        }


        public synchronized void send(Transmission transmission) {
            if (socket != null) {
                //there is a direct connection available
                try {
                    outputStream.writeObject(transmission);
                } catch (IOException e) {
                    System.err.println("could not send data: " + this.peerID);
                }
            } else {
                if (id == -1 || peerID == -1) {
                    System.err.printf("ERROR: cannot send via server, id:%d, peerOD:%d%n", id, peerID);
                }
                //no direct connection, redirect over server
                connections.get(0).send(new TransmissionTransmission(Transmission.REDIRECT_TRANSMISSION, id, peerID, transmission));
            }
        }

        public String getRemoteIP() {
            return ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress();
        }

        public boolean remotePortAvailable() {
            return remotePort != -1;
        }

        private void setPeerID(int peerID) {
            if (this.peerID != peerID) {
                if (connections.containsKey(this.peerID)) {
                    System.out.println("previous peer id: " + this.peerID);
                    System.out.println("HAVE TO REMOVE CONNECTION: NEW ID");
                    connections.remove(this.peerID);
                }
                peerSet.remove(this.peerID);

                this.peerID = peerID;

                peerSet.add(peerID);
                connections.put(peerID, this);
            }
        }

        @Override
        public void close() {
            try {
                inputStream.close();
                outputStream.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
