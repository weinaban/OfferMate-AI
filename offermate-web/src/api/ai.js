import request from '../utils/request'

export function optimizeResume(data) {
  return request({
    url: '/ai/resume/optimize',
    method: 'post',
    data,
    timeout: 120000
  })
}

export function optimizeResumeSection(data) {
  return request({
    url: '/ai/resume/optimize-section',
    method: 'post',
    data,
    timeout: 120000
  })
}

export function generateInterviewQuestions(data) {
  return request({
    url: '/ai/interview/questions',
    method: 'post',
    data,
    timeout: 120000
  })
}

export function generateJobDescription(data) {
  return request({
    url: '/ai/job/description',
    method: 'post',
    data,
    timeout: 120000
  })
}

export function normalizeAiText(data) {
  if (typeof data === 'string') {
    return data
  }

  if (Array.isArray(data)) {
    return data.map((item, index) => `${index + 1}. ${formatQuestionItem(item)}`).join('\n')
  }

  if (data && typeof data === 'object') {
    if (data.data) return normalizeAiText(data.data)
    if (data.result) return normalizeAiText(data.result)
    if (data.content) return normalizeAiText(data.content)
    if (data.description) return normalizeAiText(data.description)
    if (data.suggestion) return normalizeAiText(data.suggestion)
    if (data.text) return normalizeAiText(data.text)
    if (data.output) return normalizeAiText(data.output)
    if (data.message?.content) return normalizeAiText(data.message.content)
    if (Array.isArray(data.choices)) return normalizeAiText(data.choices)
    if (Array.isArray(data.questions)) return normalizeAiText(data.questions)
    if (typeof data.questions === 'string') return data.questions

    return JSON.stringify(data, null, 2)
  }

  return ''
}

function formatQuestionItem(item) {
  if (typeof item === 'string') {
    return item
  }

  if (item && typeof item === 'object') {
    return item.question || item.title || item.content || item.text || item.message?.content || JSON.stringify(item)
  }

  return String(item)
}

export function formatAiMarkdown(text) {
  const source = String(text || '')
    .replace(/\r\n/g, '\n')
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim()

  if (!source) {
    return ''
  }

  const lines = source.split('\n')
  const html = []
  let tableBuffer = []

  const flushTable = () => {
    if (!tableBuffer.length) return

    const rows = tableBuffer
      .filter((line) => !/^\|\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)+\|?$/.test(line.trim()))
      .map((line) => line.trim().replace(/^\|/, '').replace(/\|$/, '').split('|').map((cell) => cell.trim()))
      .filter((row) => row.some(Boolean))

    if (rows.length) {
      html.push('<table class="ai-table">')
      rows.forEach((row, index) => {
        html.push('<tr>')
        row.forEach((cell) => {
          const tag = index === 0 ? 'th' : 'td'
          html.push(`<${tag}>${inlineMarkdown(cell)}</${tag}>`)
        })
        html.push('</tr>')
      })
      html.push('</table>')
    }

    tableBuffer = []
  }

  lines.forEach((rawLine) => {
    const line = rawLine.trim()

    if (line.startsWith('|') && line.includes('|')) {
      tableBuffer.push(line)
      return
    }

    flushTable()

    if (!line) {
      html.push('<div class="ai-space"></div>')
    } else if (/^---+$/.test(line)) {
      html.push('<hr />')
    } else if (line.startsWith('### ')) {
      html.push(`<h3>${inlineMarkdown(line.replace(/^###\s+/, ''))}</h3>`)
    } else if (line.startsWith('## ')) {
      html.push(`<h2>${inlineMarkdown(line.replace(/^##\s+/, ''))}</h2>`)
    } else if (line.startsWith('# ')) {
      html.push(`<h2>${inlineMarkdown(line.replace(/^#\s+/, ''))}</h2>`)
    } else if (/^(\d+)[.、)]\s+/.test(line)) {
      html.push(`<p class="ai-question">${inlineMarkdown(line)}</p>`)
    } else if (/^[-*]\s+/.test(line)) {
      html.push(`<p class="ai-bullet">${inlineMarkdown(line.replace(/^[-*]\s+/, ''))}</p>`)
    } else {
      html.push(`<p>${inlineMarkdown(line)}</p>`)
    }
  })

  flushTable()

  return html.join('')
}

function inlineMarkdown(text) {
  return escapeHtml(text)
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
}

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}
