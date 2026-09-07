import { describe, expect, it } from 'vitest'

import source from './StationShareholderSection.vue?raw'

describe('StationShareholderSection source', () => {
  it('应渲染分区标题与关联股东入口，弹窗可打开', () => {
    expect(source).toContain('股东信息')
    expect(source).toContain('关联股东')
    expect(source).toContain('openLinkDialog')
    expect(source).toContain('linkDialogVisible')
  })

  it('关联弹窗应包含股东下拉、占股比例与关系状态，且禁止遮罩与ESC关闭', () => {
    expect(source).toContain('选择股东（主档）')
    expect(source).toContain('getShareholderLinkOptions')
    expect(source).toContain('仅展示启用状态的股东，停用股东不能新建持股关系。')
    expect(source).toContain('占股比例（%）')
    expect(source).toContain(':min="0"')
    expect(source).toContain(':max="100"')
    expect(source).toContain('持股中占比合计不等于 100% 时，保存前会提示确认。')
    expect(source).toContain('关系状态')
    expect(source).toContain('holdingStatusOptions')
    expect(source).toContain(':show-close="false"')
    expect(source).toContain(':close-on-click-modal="false"')
    expect(source).toContain(':close-on-press-escape="false"')
    expect(source).toContain('取消')
    expect(source).toContain('确认关联')
  })

  it('提交前应校验股东与占比，成功后关闭弹窗并刷新列表', () => {
    expect(source).toContain('submitLinkForm')
    expect(source).toContain('请选择股东')
    expect(source).toContain('请填写占股比例')
    expect(source).toContain('createStationShareholding')
    expect(source).toContain('关联股东成功')
    expect(source).toContain('await refresh()')
    expect(source).toContain('linkSaving')
  })

  it('股东候选加载失败应关闭弹窗并提示，错误消息透出服务端口径', () => {
    expect(source).toContain('股东选项加载失败')
    expect(source).toContain('resolveApiErrorMessage')
  })

  it('应具备加载、错误、空态与列表四种展示形态，持股行展示股东与占比信息', () => {
    expect(source).toContain('el-skeleton')
    expect(source).toContain('errorMessage')
    expect(source).toContain('重试')
    expect(source).toContain('暂无股东关系，点击右上角“关联股东”开始维护。')
    expect(source).toContain('mini-list')
    expect(source).toContain('holding.shareholderName')
    expect(source).toContain('联系人：')
    expect(source).toContain('当前占比：')
    expect(source).toContain('holding.holdingStatusName')
  })

  it('应按持股状态映射标签颜色，并对外提供刷新与摘要能力', () => {
    expect(source).toContain("case '持股中'")
    expect(source).toContain("case '已退出'")
    expect(source).toContain('resolveHoldingStatusType')
    expect(source).toContain('defineExpose({ refresh })')
    expect(source).toContain("emit('loaded'")
    expect(source).toContain('summarizeShareholdings')
    expect(source).toContain('getStationShareholdings')
  })

  it('仅当存在持股中股东且合计不等于 100% 时展示合计提示', () => {
    expect(source).toContain('ownershipSumIncomplete')
    expect(source).toContain('activeShareholderCount > 0')
    expect(source).toContain('activePercentageSum !== 100')
    expect(source).toContain('当前持股中合计')
    expect(source).toContain('不等于 100%，请确认股权结构是否符合预期。')
    expect(source).toContain('ownership-sum-warning')
  })

  it('保存前合计不等于 100% 应弹窗确认，确认后继续保存、取消则不保存', () => {
    expect(source).toContain('ElMessageBox')
    expect(source).toContain('充电站持股数合计不等于100%，是否确认。')
    expect(source).toContain("confirmButtonText: '确认'")
    expect(source).toContain("cancelButtonText: '取消'")
    expect(source).toContain("type: 'warning'")
    expect(source).toContain('closeOnClickModal: false')
    expect(source).toContain('closeOnPressEscape: false')
    expect(source).toContain('projectedSummary')
    expect(source).toContain("confirmButtonClass: 'action-button action-button--primary'")
    expect(source).toContain("cancelButtonClass: 'action-button action-button--secondary'")
  })
})