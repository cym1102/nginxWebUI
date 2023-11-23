package com.cym.utils.oshi;

import java.util.List;

/**
 * <p>
 * 网卡信息
 * </p>
 *
 * @author 皮锋
 * @custom.date 2020年3月3日 下午3:28:03
 */
public final class NetDomain  {

    /**
     * 网卡总数
     */
    private Integer netNum;
    /**
     * 网卡信息
     */
    private List<NetInterfaceDomain> netList;


    public static class NetInterfaceDomain  {
        /**
         * 网卡名字
         */
        String name;
        /**
         * 网卡类型
         */
        String type;
        /**
         * 网卡地址
         */
        String address;
        /**
         * 子网掩码
         */
        String mask;
        /**
         * 广播地址
         */
        String broadcast;
        /**
         * MAC地址
         */
        String hwAddr;
        /**
         * 网卡描述信息
         */
        String description;

        /**
         * 接收到的总字节数
         */
        Long rxBytes;

        /**
         * 接收的总包数
         */
        Long rxPackets;

        /**
         * 接收到的错误包数
         */
        Long rxErrors;

        /**
         * 接收时丢弃的包数
         */
        Long rxDropped;

        /**
         * 发送的总字节数
         */
        Long txBytes;

        /**
         * 发送的总包数
         */
        Long txPackets;

        /**
         * 发送时的错误包数
         */
        Long txErrors;

        /**
         * 发送时丢弃的包数
         */
        Long txDropped;

        /**
         * 下载速度
         */
        Double downloadBps;

        /**
         * 上传速度
         */
        Double uploadBps;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getMask() {
			return mask;
		}

		public void setMask(String mask) {
			this.mask = mask;
		}

		public String getBroadcast() {
			return broadcast;
		}

		public void setBroadcast(String broadcast) {
			this.broadcast = broadcast;
		}

		public String getHwAddr() {
			return hwAddr;
		}

		public void setHwAddr(String hwAddr) {
			this.hwAddr = hwAddr;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Long getRxBytes() {
			return rxBytes;
		}

		public void setRxBytes(Long rxBytes) {
			this.rxBytes = rxBytes;
		}

		public Long getRxPackets() {
			return rxPackets;
		}

		public void setRxPackets(Long rxPackets) {
			this.rxPackets = rxPackets;
		}

		public Long getRxErrors() {
			return rxErrors;
		}

		public void setRxErrors(Long rxErrors) {
			this.rxErrors = rxErrors;
		}

		public Long getRxDropped() {
			return rxDropped;
		}

		public void setRxDropped(Long rxDropped) {
			this.rxDropped = rxDropped;
		}

		public Long getTxBytes() {
			return txBytes;
		}

		public void setTxBytes(Long txBytes) {
			this.txBytes = txBytes;
		}

		public Long getTxPackets() {
			return txPackets;
		}

		public void setTxPackets(Long txPackets) {
			this.txPackets = txPackets;
		}

		public Long getTxErrors() {
			return txErrors;
		}

		public void setTxErrors(Long txErrors) {
			this.txErrors = txErrors;
		}

		public Long getTxDropped() {
			return txDropped;
		}

		public void setTxDropped(Long txDropped) {
			this.txDropped = txDropped;
		}

		public Double getDownloadBps() {
			return downloadBps;
		}

		public void setDownloadBps(Double downloadBps) {
			this.downloadBps = downloadBps;
		}

		public Double getUploadBps() {
			return uploadBps;
		}

		public void setUploadBps(Double uploadBps) {
			this.uploadBps = uploadBps;
		}

        
    }


	public Integer getNetNum() {
		return netNum;
	}


	public void setNetNum(Integer netNum) {
		this.netNum = netNum;
	}


	public List<NetInterfaceDomain> getNetList() {
		return netList;
	}


	public void setNetList(List<NetInterfaceDomain> netList) {
		this.netList = netList;
	}

    
    
}
