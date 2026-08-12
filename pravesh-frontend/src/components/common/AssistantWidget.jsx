import { useState, useRef, useEffect } from 'react'
import { sendAssistantMessage } from '../../api/endpoints'
import { useAuth } from '../../context/AuthContext'

const MAX_CLIENT_HISTORY = 8
const WELCOME = {
    role: 'assistant',
    text: "Hi! I'm the Pravesh Assistant. Ask me anything about creating passes, scanning visitors, guard shifts, or managing your society."
}

const SUGGESTIONS_BY_ROLE = {
    RESIDENT: ['How do I create a pass?', 'Why was my visitor denied?', 'How do I revoke a pass?'],
    GUARD: ['How do I start my shift?', 'How do I scan a pass?', 'How do I register a walk-in visitor?'],
    SOCIETY_ADMIN: ['How do I approve a resident?', 'How do I add a guard?', 'How do I view analytics?'],
    SUPER_ADMIN: ['How do I approve a new society?', 'How are duplicate societies blocked?'],
}

export default function AssistantWidget() {
    const { user } = useAuth()
    const [open, setOpen] = useState(false)
    const [messages, setMessages] = useState([WELCOME])
    const [input, setInput] = useState('')
    const [sending, setSending] = useState(false)
    const [error, setError] = useState('')
    const bodyRef = useRef(null)
    const inputRef = useRef(null)

    useEffect(() => {
        setMessages([WELCOME])
        setInput('')
        setError('')
        setOpen(false)
    }, [user?.userId])

    useEffect(() => {
        if (bodyRef.current) {
            bodyRef.current.scrollTop = bodyRef.current.scrollHeight
        }
    }, [messages, sending])

    useEffect(() => {
        if (open) setTimeout(() => inputRef.current?.focus(), 260)
    }, [open])

    if (!user) return null

    const send = async (text) => {
        if (!text || sending) return

        setError('')
        const userMsg = { role: 'user', text }
        const nextMessages = [...messages, userMsg]
        setMessages(nextMessages)
        setInput('')
        setSending(true)

        const history = nextMessages
            .filter(m => m !== WELCOME)
            .slice(-MAX_CLIENT_HISTORY - 1, -1)
            .map(m => ({ role: m.role, text: m.text }))

        try {
            const res = await sendAssistantMessage(text, history)
            const reply = res.data?.data?.reply || "Sorry, I didn't catch that — could you rephrase?"
            setMessages(prev => [...prev, { role: 'assistant', text: reply }])
        } catch (err) {
            setError('Could not reach the assistant. Please try again.')
            setMessages(prev => [...prev, {
                role: 'assistant',
                text: 'Something went wrong reaching the assistant. Please try again in a moment.'
            }])
        } finally {
            setSending(false)
        }
    }

    const handleSubmit = (e) => {
        e.preventDefault()
        send(input.trim())
    }

    const showSuggestions = messages.length === 1 && !sending
    const suggestions = SUGGESTIONS_BY_ROLE[user.role] || []

    return (
        <>
            <button
                className={`assistant-fab ${open ? 'assistant-fab-open' : ''}`}
                onClick={() => setOpen(o => !o)}
                aria-label={open ? 'Close assistant' : 'Open Pravesh Assistant'}
            >
                {open ? <i className="bi bi-x-lg"></i> : <i className="bi bi-stars"></i>}
            </button>

            {open && (
                <div className="assistant-panel">
                    <div className="assistant-header">
                        <div className="assistant-header-info">
                            <span className="assistant-avatar"><i className="bi bi-stars"></i></span>
                            <div>
                                <div className="assistant-title">Pravesh Assistant</div>
                                <div className="assistant-subtitle">
                                    <span className="assistant-live-dot"></span>Always here to help
                                </div>
                            </div>
                        </div>
                        <button className="assistant-close" onClick={() => setOpen(false)} aria-label="Close">
                            <i className="bi bi-x-lg"></i>
                        </button>
                    </div>

                    <div className="assistant-body" ref={bodyRef}>
                        {messages.map((m, i) => (
                            <div key={i} className={`assistant-msg ${m.role === 'user' ? 'assistant-msg-user' : 'assistant-msg-bot'}`}>
                                {m.role === 'assistant' && <span className="assistant-msg-avatar"><i className="bi bi-stars"></i></span>}
                                <div className="assistant-msg-bubble">{m.text}</div>
                            </div>
                        ))}

                        {showSuggestions && suggestions.length > 0 && (
                            <div className="assistant-suggestions">
                                {suggestions.map(s => (
                                    <button key={s} type="button" className="assistant-chip" onClick={() => send(s)}>
                                        {s}
                                    </button>
                                ))}
                            </div>
                        )}

                        {sending && (
                            <div className="assistant-msg assistant-msg-bot">
                                <span className="assistant-msg-avatar"><i className="bi bi-stars"></i></span>
                                <div className="assistant-msg-bubble assistant-typing">
                                    <span></span><span></span><span></span>
                                </div>
                            </div>
                        )}
                    </div>

                    {error && <div className="assistant-error">{error}</div>}

                    <form className="assistant-input-row" onSubmit={handleSubmit}>
                        <input
                            ref={inputRef}
                            type="text"
                            className="assistant-input"
                            placeholder="Ask about Pravesh..."
                            value={input}
                            onChange={e => setInput(e.target.value)}
                            disabled={sending}
                            maxLength={500}
                        />
                        <button type="submit" className="assistant-send" disabled={sending || !input.trim()}>
                            <i className="bi bi-send-fill"></i>
                        </button>
                    </form>
                </div>
            )}
        </>
    )
}