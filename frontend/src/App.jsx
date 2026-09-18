import { useMemo, useState } from 'react'

const SKILLS = [
  'python','java','javascript','typescript','react','angular','node.js','spring','spring boot',
  'fastapi','django','flask','sql','mysql','postgresql','mongodb','git','docker','aws','azure',
  'machine learning','tensorflow','pytorch','rest api','hibernate','html','css','c++','c#'
]

function percent(value) {
  return `${Number(value || 0).toFixed(2)}%`
}

function App() {
  const [files, setFiles] = useState([])
  const [jobDescription, setJobDescription] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [progress, setProgress] = useState(0)
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [history, setHistory] = useState([])
  const [historyLoading, setHistoryLoading] = useState(false)

  const jobSkills = useMemo(() => {
    const text = jobDescription.toLowerCase()
    return SKILLS.filter(skill => text.includes(skill))
  }, [jobDescription])

  function handleFiles(event) {
    const selected = Array.from(event.target.files || [])
    const valid = selected.filter(file => /\.(pdf|docx|txt)$/i.test(file.name))
    setFiles(valid)
    setError(valid.length !== selected.length ? 'Only PDF, DOCX and TXT files are supported.' : '')
    setStatus(valid.length ? `${valid.length} resume(s) selected.` : '')
  }

  function removeFile(name) {
    setFiles(current => current.filter(file => file.name !== name))
  }

  function clearAll() {
    setFiles([])
    setResults([])
    setStatus('')
    setError('')
    setProgress(0)
  }

  async function loadHistory() {
    setHistoryLoading(true)
    try {
      const response = await fetch('/api/history')
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      setHistory(await response.json())
    } catch (err) {
      setError(`Could not load PostgreSQL history: ${err.message}`)
    } finally {
      setHistoryLoading(false)
    }
  }

  async function analyze() {
    setError('')
    if (!jobDescription.trim()) {
      setError('Please enter the job description.')
      return
    }
    if (!files.length) {
      setError('Please select at least one resume.')
      return
    }

    setLoading(true)
    setProgress(0)
    const batch = []
    const failed = []

    for (let i = 0; i < files.length; i += 1) {
      const file = files[i]
      setStatus(`Analyzing ${i + 1} of ${files.length}: ${file.name}`)
      setProgress(Math.round((i / files.length) * 100))

      try {
        const form = new FormData()
        form.append('resume', file)
        form.append('job_description', jobDescription)

        const response = await fetch('/api/match-file', { method: 'POST', body: form })
        const raw = await response.text()
        let data = {}
        try { data = raw ? JSON.parse(raw) : {} } catch { throw new Error(`Invalid server response (HTTP ${response.status}).`) }
        if (!response.ok) throw new Error(data.detail || data.error || `HTTP ${response.status}`)

        batch.push({
          id: `${Date.now()}-${i}-${file.name}`,
          resumeName: file.name,
          overallScore: Number(data.overall_score || 0),
          semanticScore: Number(data.semantic_score || 0),
          tfidfScore: Number(data.tfidf_score || 0),
          skillScore: Number(data.skill_score || 0),
          matchedSkills: data.matched_skills || [],
          missingSkills: data.missing_skills || []
        })
      } catch (err) {
        failed.push(`${file.name}: ${err.message}`)
      }
      setProgress(Math.round(((i + 1) / files.length) * 100))
    }

    setResults(current => {
      const map = new Map(current.map(item => [item.resumeName, item]))
      batch.forEach(item => map.set(item.resumeName, item))
      return [...map.values()].sort((a, b) => b.overallScore - a.overallScore)
    })

    setLoading(false)
    setStatus(failed.length ? `Completed with ${failed.length} failed resume(s).` : `Successfully analyzed ${batch.length} resume(s).`)
    if (failed.length) setError(failed.join(' | '))
  }

  const topThree = results.slice(0, 3)

  return (
    <div className="page">
      <main className="app-card">
        <header className="header">
          <div className="logo">AI</div>
          <h1>AI Resume Matcher</h1>
          <p>Upload resumes and compare them with a job description.</p>
        </header>

        <section className="form-section">
          <label>Job Description</label>
          <textarea value={jobDescription} onChange={e => setJobDescription(e.target.value)} placeholder="Paste the job description here..." />

          <div className="label-row">
            <label>Upload Resumes</label>
            <span>PDF, DOCX or TXT</span>
          </div>
          <input id="resume-input" className="file-input" type="file" multiple accept=".pdf,.docx,.txt,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document,text/plain" onChange={handleFiles} />

          {files.length > 0 && (
            <div className="file-list">
              <div className="file-list-head"><strong>{files.length} resume(s) selected</strong><button type="button" onClick={() => setFiles([])}>Clear selection</button></div>
              {files.map(file => (
                <div className="file-row" key={file.name}>
                  <span title={file.name}>{file.name}</span>
                  <button type="button" aria-label={`Remove ${file.name}`} onClick={() => removeFile(file.name)}>×</button>
                </div>
              ))}
            </div>
          )}

          <button className="analyze" onClick={analyze} disabled={loading}>
            {loading ? `Analyzing ${progress}%` : 'Analyze & Match Resumes'}
          </button>

          {loading && <div className="progress"><div style={{ width: `${progress}%` }} /></div>}
          {status && <div className="status">{status}</div>}
          {error && <div className="error">{error}</div>}
        </section>

        {jobSkills.length > 0 && (
          <section className="section">
            <h2>Skills detected in the job description</h2>
            <div className="chips">{jobSkills.map(skill => <span className="chip" key={skill}>{skill}</span>)}</div>
          </section>
        )}

        {topThree.length > 0 && (
          <section className="section">
            <div className="section-title"><h2>Top 3 Best Matching Resumes</h2><span>{results.length} analyzed</span></div>
            <div className="top-grid">
              {topThree.map((item, index) => (
                <article className="result-card" key={item.id}>
                  <div className="result-head"><div><span className="rank">{index + 1}</span><strong>{item.resumeName}</strong></div><b>{percent(item.overallScore)}</b></div>
                  <div className="metric"><span>Overall</span><strong>{percent(item.overallScore)}</strong></div>
                  <div className="bar"><i style={{ width: `${Math.min(item.overallScore, 100)}%` }} /></div>
                  <div className="mini-metrics"><span>Semantic <b>{percent(item.semanticScore)}</b></span><span>TF-IDF <b>{percent(item.tfidfScore)}</b></span><span>Skills <b>{percent(item.skillScore)}</b></span></div>
                  <div className="mini-label">Matched skills</div>
                  <div className="mini-chips">{item.matchedSkills.length ? item.matchedSkills.map(skill => <span className="matched" key={skill}>{skill}</span>) : <em>None</em>}</div>
                  <div className="mini-label">Missing skills</div>
                  <div className="mini-chips">{item.missingSkills.length ? item.missingSkills.map(skill => <span className="missing" key={skill}>{skill}</span>) : <em>None</em>}</div>
                </article>
              ))}
            </div>
          </section>
        )}

        <section className="section history-section">
          <div className="section-title"><h2>Saved Analysis History</h2><button className="clear-results" onClick={loadHistory} disabled={historyLoading}>{historyLoading ? 'Loading...' : 'Load from PostgreSQL'}</button></div>
          {history.length > 0 ? (
            <div className="table-wrap">
              <table>
                <thead><tr><th>Resume</th><th>Overall</th><th>Skills</th><th>Analyzed At</th></tr></thead>
                <tbody>{history.map(item => <tr key={item.id}><td className="resume-name">{item.resumeName}</td><td><b>{percent(item.overallScore)}</b></td><td>{percent(item.skillScore)}</td><td>{new Date(item.analyzedAt).toLocaleString()}</td></tr>)}</tbody>
              </table>
            </div>
          ) : <p className="status">Click "Load from PostgreSQL" to view saved analyses.</p>}
        </section>

        {results.length > 0 && (
          <section className="section">
            <div className="section-title"><h2>All Matching Resumes</h2><span>{results.length} resumes</span></div>
            <div className="table-wrap">
              <table>
                <thead><tr><th>Rank</th><th>Resume</th><th>Overall</th><th>Text Similarity</th><th>Skill Coverage</th></tr></thead>
                <tbody>{results.map((item, index) => <tr key={item.id}><td>{index + 1}</td><td className="resume-name">{item.resumeName}</td><td><b>{percent(item.overallScore)}</b></td><td>{percent(item.tfidfScore)}</td><td>{percent(item.skillScore)}</td></tr>)}</tbody>
              </table>
            </div>
            <button className="clear-results" onClick={clearAll}>Clear results</button>
          </section>
        )}
      </main>
    </div>
  )
}

export default App

