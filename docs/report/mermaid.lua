function CodeBlock(block)
  if block.classes:includes('mermaid') then
    local text = block.text
      :gsub('&', '&amp;')
      :gsub('<', '&lt;')
      :gsub('>', '&gt;')
    return pandoc.RawBlock('html', '<div class="mermaid">\n' .. text .. '\n</div>')
  end
end
