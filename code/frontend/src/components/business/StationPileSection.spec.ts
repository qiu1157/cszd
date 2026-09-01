import { describe, expect, it } from 'vitest'

import source from './StationPileSection.vue?raw'

describe('StationPileSection source', () => {
  it('应渲染分区标题与新增入口，新增弹窗可打开', () => {
    expect(source).toContain('充电桩管理')
    expect(source).toContain('新增充电桩')
    expect(source).toContain('openCreateDialog')
    expect(source).toContain('dialogVisible')
  })

  it('新增弹窗应包含只读桩编号、充电类型与枪数字段，且禁止遮罩与ESC关闭', () => {
    expect(source).toContain('桩编号')
    expect(source).toContain('nextPileCode')
    expect(source).toContain('disabled')
    expect(source).toContain('桩编号由系统自动生成')
    expect(source).toContain('充电类型')
    expect(source).toContain('CHARGE_TYPE_OPTIONS')
    expect(source).toContain('充电枪数量')
    expect(source).toContain(':min="1"')
    expect(source).toContain('新增后默认状态为运营中')
    expect(source).toContain(':show-close="false"')
    expect(source).toContain(':close-on-click-modal="false"')
    expect(source).toContain(':close-on-press-escape="false"')
    expect(source).toContain('取消')
    expect(source).toContain('保存')
  })

  it('保存前应校验类型与枪数，成功后关闭弹窗并刷新列表', () => {
    expect(source).toContain('submitPileForm')
    expect(source).toContain('请选择充电类型')
    expect(source).toContain('充电枪数量至少为1')
    expect(source).toContain('createStationPile')
    expect(source).toContain('getStationNextPileCode')
    expect(source).toContain('新增充电桩成功')
    expect(source).toContain('await refresh()')
    expect(source).toContain('dialogSaving')
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
