export function normalizeAiText(data) {
  if (data === null || data === undefined) return ''

  if (typeof data === 'string') return data

  if (Array.isArray(data)) {
    return data.map((item, index) => `${index + 1}. ${normalizeAiText(item)}`).join('\n')
  }

  if (typeof data === 'object') {
    const keys = [
      'result',
      'content',
      'description',
      'suggestion',
      'suggestions',
      'advice',
      'text',
      'summary',
      'analysis',
      'message',
      'output'
    ]

    for (const key of keys) {
      if (data[key]) return normalizeAiText(data[key])
    }

    if (Array.isArray(data.questions)) return normalizeAiText(data.questions)
    if (data.data) return normalizeAiText(data.data)

    return JSON.stringify(data, null, 2)
  }

  return String(data)
}

export function normalizeAiList(value) {
  if (!value) return []

  if (Array.isArray(value)) {
    return value
      .map((item) => {
        if (typeof item === 'string') return item.trim()
        if (item && typeof item === 'object') {
          return item.content || item.text || item.title || item.name || normalizeAiText(item)
        }
        return String(item)
      })
      .filter(Boolean)
  }

  if (typeof value === 'string') {
    return value
      .replace(/\r\n/g, '\n')
      .split(/\n|；|;/)
      .map((item) => item.replace(/^[-*]\s*/, '').replace(/^\d+[.、]\s*/, '').trim())
      .filter(Boolean)
  }

  if (typeof value === 'object') return normalizeAiList(normalizeAiText(value))

  return [String(value)]
}

export function normalizeMatchResult(data) {
  const source = unwrap(data)
  const score = Number(source.score ?? source.matchScore ?? source.match_score ?? 0)

  return {
    score: Number.isFinite(score) ? Math.max(0, Math.min(100, score)) : 0,
    advantages: normalizeAiList(source.advantages || source.strengths || source.advantage),
    weaknesses: normalizeAiList(source.weaknesses || source.shortcomings || source.gaps || source.shortboard),
    suggestions: normalizeAiList(source.suggestions || source.advice || source.improvementSuggestions || source.improvements),
    learningPath: normalizeAiList(source.learningPath || source.learning_path || source.learning || source.studyPlan),
    text: normalizeAiText(source.result || source.content || source.text || (isPlainObject(source) ? '' : source))
  }
}

export function normalizeInterviewQuestion(data) {
  const source = unwrap(data)

  if (typeof source === 'string') return source

  if (Array.isArray(source)) return normalizeAiText(source)

  return (
    source.question ||
    source.followUpQuestion ||
    source.followUp ||
    source.title ||
    source.content ||
    source.text ||
    source.result ||
    normalizeAiText(source)
  )
}

export function normalizeInterviewAnswer(data) {
  const source = unwrap(data)
  const score = source.score ?? source.rating ?? source.grade ?? ''

  return {
    score,
    followUp: source.followUp || source.followUpQuestion || source.question || '',
    suggestion: source.suggestion || source.advice || source.analysis || source.result || source.content || source.text || '',
    finished: source.finished === true,
    text: normalizeAiText(source.result || source.content || source.text || source.analysis || '')
  }
}

export function normalizeInterviewReport(data) {
  const source = unwrap(data)
  const score = source.overallScore ?? source.score ?? source.rating ?? ''

  return {
    score,
    summary: normalizeAiText(source.summary || source.result || source.content || source.text || ''),
    advantages: normalizeAiList(source.advantages || source.strengths),
    weaknesses: normalizeAiList(source.weaknesses || source.shortcomings || source.gaps),
    suggestions: normalizeAiList(source.suggestions || source.advice || source.improvementSuggestions),
    questions: normalizeAiList(source.questions),
    rawText: normalizeAiText(source)
  }
}

function unwrap(data) {
  if (data && typeof data === 'object' && data.data) return unwrap(data.data)
  return data || {}
}

function isPlainObject(value) {
  return Object.prototype.toString.call(value) === '[object Object]'
}
