import { useState, useEffect, useCallback } from 'react'
import {
  listForumPosts, createForumPost, listForumComments,
  addForumComment, toggleForumPin, deleteForumPost
} from '../../api/endpoints'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import BackButton from '../../components/common/BackButton'

const CATEGORIES = ['GENERAL', 'MAINTENANCE', 'EVENTS']

export default function ForumPage() {
  const { user } = useAuth()
  const { showToast } = useToast()
  const isAdmin = user?.role === 'SOCIETY_ADMIN'

  const [activeCategory, setActiveCategory] = useState('ALL')
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)

  const [expandedId, setExpandedId] = useState(null)
  const [comments, setComments] = useState({}) // postId -> comments array
  const [loadingComments, setLoadingComments] = useState(false)
  const [commentDraft, setCommentDraft] = useState('')
  const [postingComment, setPostingComment] = useState(false)

  const [showNewPost, setShowNewPost] = useState(false)
  const [newCategory, setNewCategory] = useState('GENERAL')
  const [newTitle, setNewTitle] = useState('')
  const [newBody, setNewBody] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const res = await listForumPosts(activeCategory === 'ALL' ? undefined : activeCategory)
      setPosts(res.data.data || [])
    } catch {
      setPosts([])
    } finally {
      setLoading(false)
    }
  }, [activeCategory])

  useEffect(() => { load() }, [load])

  const toggleExpand = async (postId) => {
    if (expandedId === postId) {
      setExpandedId(null)
      return
    }
    setExpandedId(postId)
    if (!comments[postId]) {
      setLoadingComments(true)
      try {
        const res = await listForumComments(postId)
        setComments(prev => ({ ...prev, [postId]: res.data.data || [] }))
      } catch {
        setComments(prev => ({ ...prev, [postId]: [] }))
      } finally {
        setLoadingComments(false)
      }
    }
  }

  const handlePostComment = async (postId) => {
    if (!commentDraft.trim()) return
    setPostingComment(true)
    try {
      await addForumComment(postId, commentDraft.trim())
      setCommentDraft('')
      const res = await listForumComments(postId)
      setComments(prev => ({ ...prev, [postId]: res.data.data || [] }))
      load() // refresh comment counts on the post cards
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not post comment.', 'error')
    } finally {
      setPostingComment(false)
    }
  }

  const handlePin = async (postId) => {
    try {
      await toggleForumPin(postId)
      load()
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not update pin status.', 'error')
    }
  }

  const handleDelete = async (postId) => {
    if (!window.confirm('Remove this post? This cannot be undone from the UI.')) return
    try {
      await deleteForumPost(postId)
      showToast('Post removed', 'success')
      load()
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not remove post.', 'error')
    }
  }

  const handleNewPost = async (e) => {
    e.preventDefault()
    if (!newTitle.trim() || !newBody.trim()) return
    setSubmitting(true)
    try {
      await createForumPost(newCategory, newTitle.trim(), newBody.trim())
      showToast('Post created!', 'success')
      setShowNewPost(false)
      setNewTitle(''); setNewBody(''); setNewCategory('GENERAL')
      load()
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not create post.', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      <Navbar />
      <div className="container py-4">
        <BackButton to="/dashboard" label="Back to Society Dashboard" />
        <div className="page-header d-flex justify-content-between align-items-center flex-wrap gap-2">
          <div>
            <h4 className="mb-1"><i className="bi bi-chat-square-text-fill me-2"></i>Community Forum</h4>
            <p className="mb-0 opacity-75">Posts, updates, and discussions from your society</p>
          </div>
          <button className="btn btn-sm btn-pravesh" onClick={() => setShowNewPost(true)}>
            <i className="bi bi-plus-circle me-1"></i>New Post
          </button>
        </div>

        {/* Category tabs */}
        <ul className="nav nav-pills mt-3 mb-3 gap-2">
          {['ALL', ...CATEGORIES].map(cat => (
            <li className="nav-item" key={cat}>
              <button
                className={`nav-link ${activeCategory === cat ? 'active' : ''}`}
                onClick={() => setActiveCategory(cat)}
              >
                {cat === 'ALL' ? 'All Posts' : cat.charAt(0) + cat.slice(1).toLowerCase()}
              </button>
            </li>
          ))}
        </ul>

        {loading ? (
          <LoadingSpinner text="Loading posts..." />
        ) : posts.length === 0 ? (
          <div className="card p-4 text-center text-muted">
            No posts in this category yet. Be the first!
          </div>
        ) : (
          <div className="d-flex flex-column gap-3">
            {posts.map(p => (
              <div key={p.id} className="card p-3">
                <div className="d-flex justify-content-between align-items-start">
                  <div className="flex-grow-1">
                    <div className="d-flex align-items-center gap-2 mb-1">
                      {p.pinned && <i className="bi bi-pin-angle-fill text-warning"></i>}
                      <h6 className="fw-bold mb-0">{p.title}</h6>
                      <span className="badge bg-secondary">{p.category}</span>
                    </div>
                    <p className="small mb-2" style={{ whiteSpace: 'pre-wrap' }}>{p.body}</p>
                    <div className="text-muted small">
                      {p.authorName || 'Unknown'} &middot; {new Date(p.createdAt).toLocaleString()}
                    </div>
                  </div>

                  {isAdmin && (
                    <div className="d-flex gap-1 flex-shrink-0 ms-2">
                      <button
                        className={`btn btn-sm ${p.pinned ? 'btn-warning' : 'btn-outline-warning'}`}
                        title={p.pinned ? 'Unpin' : 'Pin'}
                        onClick={() => handlePin(p.id)}
                      >
                        <i className="bi bi-pin-angle-fill"></i>
                      </button>
                      <button
                        className="btn btn-sm btn-outline-danger"
                        title="Delete"
                        onClick={() => handleDelete(p.id)}
                      >
                        <i className="bi bi-trash"></i>
                      </button>
                    </div>
                  )}
                </div>

                <button
                  className="btn btn-sm btn-link text-decoration-none p-0 mt-2 text-start"
                  onClick={() => toggleExpand(p.id)}
                >
                  <i className={`bi bi-chat-dots me-1`}></i>
                  {p.commentCount} comment{p.commentCount !== 1 ? 's' : ''}
                  {expandedId === p.id ? ' — hide' : ' — view'}
                </button>

                {expandedId === p.id && (
                  <div className="mt-3 pt-3 border-top border-secondary border-opacity-25">
                    {loadingComments ? (
                      <p className="text-muted small">Loading comments...</p>
                    ) : (comments[p.id]?.length ?? 0) === 0 ? (
                      <p className="text-muted small mb-3">No comments yet.</p>
                    ) : (
                      <div className="mb-3" style={{ maxHeight: 240, overflowY: 'auto' }}>
                        {comments[p.id].map(c => (
                          <div key={c.id} className="border-bottom pb-2 mb-2">
                            <div className="d-flex justify-content-between">
                              <span className="fw-semibold small">{c.authorName || 'Unknown'}</span>
                              <span className="text-muted small">{new Date(c.createdAt).toLocaleString()}</span>
                            </div>
                            <div className="small">{c.body}</div>
                          </div>
                        ))}
                      </div>
                    )}
                    <div className="d-flex gap-2">
                      <input
                        className="form-control form-control-sm"
                        placeholder="Add a comment..."
                        value={commentDraft}
                        onChange={e => setCommentDraft(e.target.value)}
                        onKeyDown={e => e.key === 'Enter' && handlePostComment(p.id)}
                      />
                      <button
                        className="btn btn-sm btn-pravesh flex-shrink-0"
                        style={{ whiteSpace: 'nowrap' }}
                        disabled={postingComment || !commentDraft.trim()}
                        onClick={() => handlePostComment(p.id)}
                      >
                        Post
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {showNewPost && (
        <div className="modal d-block" style={{ background: 'rgba(3,6,12,0.7)' }}
          tabIndex="-1" onClick={() => setShowNewPost(false)}>
          <div className="modal-dialog modal-dialog-centered" onClick={e => e.stopPropagation()}>
            <div className="modal-content sos-modal">
              <form onSubmit={handleNewPost}>
                <div className="modal-header border-0">
                  <h5 className="modal-title fw-bold">
                    <i className="bi bi-chat-square-text-fill me-2"></i>New Post
                  </h5>
                  <button type="button" className="btn-close" onClick={() => setShowNewPost(false)}></button>
                </div>
                <div className="modal-body">
                  <label className="form-label small fw-semibold">Category</label>
                  <select
                    className="form-select mb-3"
                    value={newCategory}
                    onChange={e => setNewCategory(e.target.value)}
                  >
                    {CATEGORIES.map(c => (
                      <option key={c} value={c}>{c.charAt(0) + c.slice(1).toLowerCase()}</option>
                    ))}
                  </select>
                  <label className="form-label small fw-semibold">Title</label>
                  <input
                    className="form-control mb-3"
                    value={newTitle}
                    onChange={e => setNewTitle(e.target.value)}
                    maxLength={150}
                    required
                  />
                  <label className="form-label small fw-semibold">Body</label>
                  <textarea
                    className="form-control"
                    rows={4}
                    value={newBody}
                    onChange={e => setNewBody(e.target.value)}
                    required
                  />
                </div>
                <div className="modal-footer border-0">
                  <button type="button" className="btn btn-outline-secondary" onClick={() => setShowNewPost(false)} disabled={submitting}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-pravesh fw-bold" disabled={submitting}>
                    {submitting
                      ? <><span className="spinner-border spinner-border-sm me-2"></span>Posting...</>
                      : 'Post'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </>
  )
}