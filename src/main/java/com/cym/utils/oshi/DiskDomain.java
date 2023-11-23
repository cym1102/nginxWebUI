package com.cym.utils.oshi;

import java.util.List;

/**
 * <p>
 * 磁盘信息
 * </p>
 *
 * @author 皮锋
 * @custom.date 2020/3/8 20:35
 */
public class DiskDomain {

	/**
	 * 磁盘数量
	 */
	private int diskNum;

	/**
	 * 磁盘信息
	 */
	private List<DiskInfoDomain> diskInfoList;

	public static class DiskInfoDomain {

		/**
		 * 分区的盘符名称
		 */
		String devName;

		/**
		 * 分区的盘符路径
		 */
		String dirName;

		/**
		 * 文件系统类型名，比如本地硬盘、光驱、网络文件系统等
		 */
		String typeName;

		/**
		 * 文件系统类型，比如 FAT32、NTFS
		 */
		String sysTypeName;

		/**
		 * 文件系统总大小（单位：byte）
		 */
		Long total;

		/**
		 * 文件系统剩余大小（单位：byte）
		 */
		Long free;

		/**
		 * 文件系统已使用大小（单位：byte）
		 */
		Long used;

		/**
		 * 文件系统可用大小（单位：byte）
		 */
		Long avail;

		/**
		 * 文件系统资源的利用率
		 */
		Double usePercent;

		public String getDevName() {
			return devName;
		}

		public void setDevName(String devName) {
			this.devName = devName;
		}

		public String getDirName() {
			return dirName;
		}

		public void setDirName(String dirName) {
			this.dirName = dirName;
		}

		public String getTypeName() {
			return typeName;
		}

		public void setTypeName(String typeName) {
			this.typeName = typeName;
		}

		public String getSysTypeName() {
			return sysTypeName;
		}

		public void setSysTypeName(String sysTypeName) {
			this.sysTypeName = sysTypeName;
		}

		public Long getTotal() {
			return total;
		}

		public void setTotal(Long total) {
			this.total = total;
		}

		public Long getFree() {
			return free;
		}

		public void setFree(Long free) {
			this.free = free;
		}

		public Long getUsed() {
			return used;
		}

		public void setUsed(Long used) {
			this.used = used;
		}

		public Long getAvail() {
			return avail;
		}

		public void setAvail(Long avail) {
			this.avail = avail;
		}

		public Double getUsePercent() {
			return usePercent;
		}

		public void setUsePercent(Double usePercent) {
			this.usePercent = usePercent;
		}

	}

	public int getDiskNum() {
		return diskNum;
	}

	public void setDiskNum(int diskNum) {
		this.diskNum = diskNum;
	}

	public List<DiskInfoDomain> getDiskInfoList() {
		return diskInfoList;
	}

	public void setDiskInfoList(List<DiskInfoDomain> diskInfoList) {
		this.diskInfoList = diskInfoList;
	}

}
