import { describe, expect, it } from 'vitest'

import source from './SystemDictionaryListView.vue?raw'

describe('SystemDictionaryListView source', () => {
  it('应改为顶部工具栏字典类型下拉布局，并移除旧说明区与左侧导航', () => {
    expect(source).toContain('<label>字典类型</label>')
    expect(source).toContain('<el-select')
    expect(source).toContain('dictionary-toolbar')
    expect(source).toContain('新增字典项')
    expect(source).toContain('<el-dialog')
    expect(source).toContain('新增字典项</h3>')
    expect(source).toContain('编辑字典项</h3>')
    expect(source).toContain('@click="openCreateDialog"')
    expect(source).toContain('@click="openEditDialog(row)"')
    expect(source).toContain(`@click="submitItemStatus(row, 'enable')"`)
    expect(source).toContain(`@click="submitItemStatus(row, 'disable')"`)
    expect(source).toContain('ElMessageBox.confirm')
    expect(source).toContain(':close-on-click-modal="false"')
    expect(source).toContain(':close-on-press-escape="false"')

    expect(source).not.toContain('summary-row')
    expect(source).not.toContain('summary-chip')
    expect(source).not.toContain('dictionary-nav')
    expect(source).not.toContain('当前仅提供列表查看能力')
  })
})
