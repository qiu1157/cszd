import { describe, expect, it } from 'vitest'

import source from './StationCreateView.vue?raw'

describe('StationCreateView source', () => {
  it('应渲染新增充电站表单，且初始状态固定为签合同', () => {
    expect(source).toContain('新增充电站')
    expect(source).toContain('保存并返回列表')
    expect(source).toContain('所在平台')
    expect(source).toContain('签合同（默认）')
    expect(source).toContain('充电站名称')
  })
})
