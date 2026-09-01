import { describe, expect, it } from 'vitest'

import source from './StationPileSection.vue?raw'

describe('StationPileSection source', () => {
  it('应渲染分区标题与新增入口，新增弹窗未交付前给出明确引导', () => {
    expect(source).toContain('充电桩管理')
    expect(source).toContain('新增充电桩')
    expect(source).toContain('handleCreateClick')
    expect(source).toContain('ElMessage.info')
  })

  it('应具备加载、错误、空态与列表四种展示形态', () => {
    expect(source).toContain('el-skeleton')
    expect(source).toContain('errorMessage')
    expect(source).toContain('重试')
    expect(source).toContain('暂无充电桩，点击右上角“新增充电桩”开始维护。')
    expect(source).toContain('mini-list')
    expect(source).toContain('pile.pileCode')
    expect(source).toContain('pile.chargeTypeName')
    expect(source).toContain('把枪')
    expect(source).toContain('pile.pileStatusName')
  })

  it('应按桩状态语义映射标签颜色，并对外提供刷新与统计能力', () => {
    expect(source).toContain("case '运营中'")
    expect(source).toContain("case '运营异常'")
    expect(source).toContain("case '停用'")
    expect(source).toContain('resolvePileStatusType')
    expect(source).toContain('defineExpose({ refresh })')
    expect(source).toContain("emit('loaded'")
    expect(source).toContain('summarizePiles')
    expect(source).toContain('getStationPiles')
  })
})
