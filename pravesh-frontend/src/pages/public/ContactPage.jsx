import { useState } from 'react'
import Navbar from '../../components/common/Navbar'
import { useScrollReveal } from '../../hooks/useScrollReveal'
import './ContactPage.css'

const CONTACT_EMAIL = 'pravesh.notify@gmail.com'

export default function ContactPage() {
  useScrollReveal()

  const [form, setForm] = useState({ name: '', email: '', subject: '', message: '' })
  const [submitted, setSubmitted] = useState(false)

  const handleChange = (field) => (e) => {
    setForm(f => ({ ...f, [field]: e.target.value }))
  }

  const handleSubmit = (e) => {
    e.preventDefault()

    const bodyText =
      `Name: ${form.name}\nEmail: ${form.email}\n\n${form.message}`

    const gmailComposeUrl =
      `https://mail.google.com/mail/?view=cm&fs=1&to=${encodeURIComponent(CONTACT_EMAIL)}` +
      `&su=${encodeURIComponent(form.subject)}` +
      `&body=${encodeURIComponent(bodyText)}`

    window.open(gmailComposeUrl, '_blank', 'noopener,noreferrer')
    setSubmitted(true)
  }

  return (
    <>
      <Navbar />
      <div className="contact-page pv-page">

        <section className="pv-banner">
          <div className="pv-banner-aurora" aria-hidden="true"><span></span><span></span></div>
          <div className="pv-banner-noise" aria-hidden="true"></div>
          <div className="pv-banner-dots" aria-hidden="true"></div>
          <div className="pv-banner-inner">
            <div className="pv-pill"><span className="pv-pill-dot"></span>Contact</div>
            <h1>Let's talk about<br /><span className="pv-glow">your community</span>.</h1>
            <p>Questions, a demo, or help getting set up — we usually reply within 24 hours.</p>
          </div>
        </section>

        <div className="contact-body">
        <div className="row g-4">

          <div className="col-md-6">
            <div className="contact-info pv-card" data-reveal>
              <h2 className="mb-3">Get in Touch</h2>
              <p className="para mb-4">
                Questions about Pravesh, want a demo for your society, or found a bug?
                We'd love to hear from you.
              </p>

              <div className="contact-item mb-3">
                <h5><i className="bi bi-geo-alt-fill me-2"></i>Address</h5>
                <p className="para">Mumbai, Kharghar</p>
              </div>
              <div className="contact-item mb-3">
                <h5><i className="bi bi-telephone-fill me-2"></i>Phone</h5>
                <p className="para">+91 9766404729</p>
              </div>
              <div className="contact-item mb-3">
                <h5><i className="bi bi-envelope-fill me-2"></i>Email</h5>
                <a
                  href={`https://mail.google.com/mail/?view=cm&fs=1&to=${encodeURIComponent(CONTACT_EMAIL)}`}
                  target="_blank" rel="noreferrer" className="para contact-link"
                >
                  {CONTACT_EMAIL}
                </a>
              </div>
              <div className="contact-item">
                <h5><i className="bi bi-clock-fill me-2"></i>Working Hours</h5>
                <p className="para">Monday – Friday: 9:00 AM – 6:00 PM</p>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="contact-form pv-card" data-reveal>
              {!submitted ? (
                <form onSubmit={handleSubmit}>
                  <h2 className="mb-4">Send a Message</h2>

                  <div className="mb-3">
                    <label className="form-label">Your Name</label>
                    <input type="text" className="form-control custom-pravesh-input" placeholder="Enter your name"
                      value={form.name} onChange={handleChange('name')} required />
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Email Address</label>
                    <input type="email" className="form-control custom-pravesh-input" placeholder="you@example.com"
                      value={form.email} onChange={handleChange('email')} required />
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Subject</label>
                    <input type="text" className="form-control custom-pravesh-input" placeholder="How can we help?"
                      value={form.subject} onChange={handleChange('subject')} required />
                  </div>
                  <div className="mb-4">
                    <label className="form-label">Message</label>
                    <textarea className="form-control custom-pravesh-input" rows={4} placeholder="Your message..."
                      value={form.message} onChange={handleChange('message')} required></textarea>
                  </div>

                  <button type="submit" className="pv-btn-glow w-100 justify-content-center">
                    <i className="bi bi-send-fill me-2"></i>Send Message
                  </button>
                </form>
              ) : (
                <div id="thanks-pravesh" className="text-center">
                  <i className="bi bi-check-circle-fill" style={{ fontSize: '3rem', color: '#198754' }}></i>
                  <h2 className="mt-3 mb-2">Almost there!</h2>
                  <p className="para">
                    We've opened Gmail in a new tab with your message pre-filled — just hit send from there to reach us.
                  </p>
                </div>
              )}
            </div>
          </div>

        </div>
        </div>
      </div>
    </>
  )
}