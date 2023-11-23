package com.cym.utils.oshi;

import java.util.List;

/**
 * <p>
 * CPU信息
 * </p>
 *
 * @author 皮锋
 * @custom.date 2020年3月3日 下午2:28:02
 */
public class CpuDomain  {

    /**
     * cpu总数
     */
    private Integer cpuNum;
    /**
     * cpu信息
     */
    private List<CpuInfoDomain> cpuList;

    public static class CpuInfoDomain {

        /**
         * CPU频率（MHz）
         */
        Integer cpuMhz;

        /**
         * CPU卖主
         */
        String cpuVendor;

        /**
         * CPU的类别，如：Celeron
         */
        String cpuModel;

        /**
         * CPU用户使用率
         */
        Double cpuUser;

        /**
         * CPU系统使用率
         */
        Double cpuSys;

        /**
         * CPU等待率
         */
        Double cpuWait;

        /**
         * CPU错误率
         */
        Double cpuNice;

        /**
         * CPU剩余率
         */
        Double cpuIdle;

        /**
         * CPU使用率
         */
        Double cpuCombined;

		public Integer getCpuMhz() {
			return cpuMhz;
		}

		public void setCpuMhz(Integer cpuMhz) {
			this.cpuMhz = cpuMhz;
		}

		public String getCpuVendor() {
			return cpuVendor;
		}

		public void setCpuVendor(String cpuVendor) {
			this.cpuVendor = cpuVendor;
		}

		public String getCpuModel() {
			return cpuModel;
		}

		public void setCpuModel(String cpuModel) {
			this.cpuModel = cpuModel;
		}

		public Double getCpuUser() {
			return cpuUser;
		}

		public void setCpuUser(Double cpuUser) {
			this.cpuUser = cpuUser;
		}

		public Double getCpuSys() {
			return cpuSys;
		}

		public void setCpuSys(Double cpuSys) {
			this.cpuSys = cpuSys;
		}

		public Double getCpuWait() {
			return cpuWait;
		}

		public void setCpuWait(Double cpuWait) {
			this.cpuWait = cpuWait;
		}

		public Double getCpuNice() {
			return cpuNice;
		}

		public void setCpuNice(Double cpuNice) {
			this.cpuNice = cpuNice;
		}

		public Double getCpuIdle() {
			return cpuIdle;
		}

		public void setCpuIdle(Double cpuIdle) {
			this.cpuIdle = cpuIdle;
		}

		public Double getCpuCombined() {
			return cpuCombined;
		}

		public void setCpuCombined(Double cpuCombined) {
			this.cpuCombined = cpuCombined;
		}

        
    }

	public Integer getCpuNum() {
		return cpuNum;
	}

	public void setCpuNum(Integer cpuNum) {
		this.cpuNum = cpuNum;
	}

	public List<CpuInfoDomain> getCpuList() {
		return cpuList;
	}

	public void setCpuList(List<CpuInfoDomain> cpuList) {
		this.cpuList = cpuList;
	}

    
}
