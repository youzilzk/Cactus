package com.youzi.tunnel.client.entity;




import java.io.Serializable;


public class Tunnel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /* 隧道名称 */
    private String name;

    /* 源端口 */
    private Integer fromPort;
    /* 源主机 */
    private String fromHost;
    /* 源端口 */
    private String fromClient;
    /* 到端口 */
    private Integer toPort;
    /* 到客户端 */
    private String toClient;

    public Tunnel() {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFromPort() {
        return fromPort;
    }

    public void setFromPort(Integer fromPort) {
        this.fromPort = fromPort;
    }

    public String getFromHost() {
        return fromHost;
    }

    public void setFromHost(String fromHost) {
        this.fromHost = fromHost;
    }

    public String getFromClient() {
        return fromClient;
    }

    public void setFromClient(String fromClient) {
        this.fromClient = fromClient;
    }

    public Integer getToPort() {
        return toPort;
    }

    public void setToPort(Integer toPort) {
        this.toPort = toPort;
    }

    public String getToClient() {
        return toClient;
    }

    public void setToClient(String toClient) {
        this.toClient = toClient;
    }

    public Tunnel copy() {
        Tunnel tunnel = new Tunnel();
        tunnel.setId(id);
        tunnel.setName(name);
        tunnel.setFromPort(fromPort);
        tunnel.setFromHost(fromHost);
        tunnel.setFromClient(fromClient);
        tunnel.setToPort(toPort);
        tunnel.setToClient(toClient);
        return tunnel;
    }

    @Override
    public String toString() {
        return "Tunnel{" +
                "name='" + name + '\'' +
                ", fromPort=" + fromPort +
                ", fromHost='" + fromHost + '\'' +
                ", fromClient='" + fromClient + '\'' +
                ", toPort=" + toPort +
                ", toClient='" + toClient + '\'' +
                '}';
    }
}
