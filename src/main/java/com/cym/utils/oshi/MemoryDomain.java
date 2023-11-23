package com.cym.utils.oshi;

/**
 * <p>
 * 内存信息
 * </p>
 *
 * @author 皮锋
 * @custom.date 2020年3月3日 下午2:20:14
 */
public class MemoryDomain {

	/**
	 * 内存信息
	 */
	private MenDomain menDomain;

	/**
	 * 交换区信息
	 */
	private SwapDomain swapDomain;

	public static class MenDomain {

		/**
		 * 物理内存总量（单位：byte）
		 */
		private Long memTotal;

		/**
		 * 物理内存使用量（单位：byte）
		 */
		private Long memUsed;

		/**
		 * 物理内存剩余量（单位：byte）
		 */
		private Long memFree;

		/**
		 * 物理内存使用率
		 */
		private Double menUsedPercent;

		public Long getMemTotal() {
			return memTotal;
		}

		public void setMemTotal(Long memTotal) {
			this.memTotal = memTotal;
		}

		public Long getMemUsed() {
			return memUsed;
		}

		public void setMemUsed(Long memUsed) {
			this.memUsed = memUsed;
		}

		public Long getMemFree() {
			return memFree;
		}

		public void setMemFree(Long memFree) {
			this.memFree = memFree;
		}

		public Double getMenUsedPercent() {
			return menUsedPercent;
		}

		public void setMenUsedPercent(Double menUsedPercent) {
			this.menUsedPercent = menUsedPercent;
		}

	}

	public static class SwapDomain {

		/**
		 * 交换区总量（单位：byte）
		 */
		private Long swapTotal;

		/**
		 * 交换区使用量（单位：byte）
		 */
		private Long swapUsed;

		/**
		 * 交换区剩余量（单位：byte）
		 */
		private Long swapFree;

		/**
		 * 交换区使用率
		 */
		private Double swapUsedPercent;

		public Long getSwapTotal() {
			return swapTotal;
		}

		public void setSwapTotal(Long swapTotal) {
			this.swapTotal = swapTotal;
		}

		public Long getSwapUsed() {
			return swapUsed;
		}

		public void setSwapUsed(Long swapUsed) {
			this.swapUsed = swapUsed;
		}

		public Long getSwapFree() {
			return swapFree;
		}

		public void setSwapFree(Long swapFree) {
			this.swapFree = swapFree;
		}

		public Double getSwapUsedPercent() {
			return swapUsedPercent;
		}

		public void setSwapUsedPercent(Double swapUsedPercent) {
			this.swapUsedPercent = swapUsedPercent;
		}

	}

	public MenDomain getMenDomain() {
		return menDomain;
	}

	public void setMenDomain(MenDomain menDomain) {
		this.menDomain = menDomain;
	}

	public SwapDomain getSwapDomain() {
		return swapDomain;
	}

	public void setSwapDomain(SwapDomain swapDomain) {
		this.swapDomain = swapDomain;
	}

}
