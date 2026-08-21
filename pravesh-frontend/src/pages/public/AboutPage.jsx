import { Link } from 'react-router-dom'
import Navbar from '../../components/common/Navbar'
import { useScrollReveal } from '../../hooks/useScrollReveal'
import logoMark from '../../assets/logo.png'
import './AboutPage.css'
import varadImg from '../../assets/varad.jpg';
import snehaImg from '../../assets/Sneha.png';
import saloneeImg from '../../assets/Salonee.png';
import shreyaImg from '../../assets/Shreya.png';
import vyankiiImg from '../../assets/vyankii.jpeg';

/* ── Default Avatar SVG (initials) ── */
const DefaultAvatar = ({ name }) => {
  const initials = name.split(' ').slice(0, 2).map(w => w[0]).join('')
  const uid = `grad-${initials}-${Math.random().toString(36).slice(2, 6)}`
  return (
    <svg viewBox="0 0 80 80" xmlns="http://www.w3.org/2000/svg" className="default-avatar">
      <defs>
        <linearGradient id={uid} x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#2c5d8c" />
          <stop offset="100%" stopColor="#e8871a" />
        </linearGradient>
      </defs>
      <circle cx="40" cy="40" r="40" fill={`url(#${uid})`} />
      <text x="40" y="47" textAnchor="middle" fontSize="24" fontWeight="700"
        fontFamily="Segoe UI, sans-serif" fill="white" letterSpacing="1">
        {initials}
      </text>
    </svg>
  )
}

/* ── Social Icons ── */
const SocialIcon = ({ href, icon, title }) => (
  <a href={href} target="_blank" rel="noreferrer" title={title} className="social-link-pravesh">
    <i className={`bi ${icon}`}></i>
  </a>
)

const teamMembers = [
  {
    id: 1,
    name: 'Salonee Pravin Shirsat',
    role: 'Backend Developer',
    photo: saloneeImg,
    bio: 'Built the Pass and Validation modules in the Spring Boot monolith, implementing QR code generation with ZXing and race-condition-free scan validation using MySQL Pessimistic Locking (SELECT FOR UPDATE). Applied Jakarta Bean Validation and Global Exception Handling across the platform to ensure clean, consistent API responses.',
    email: 'shirsatsalonee510@gmail.com',
    linkedin: 'https://www.linkedin.com/in/salonee-shirsat-325517248',
    github: 'https://github.com/89285-Salonee',
  },
  {
    id: 2,
    name: 'Shreya Jangid',
    role: 'Backend Developer',
    photo: shreyaImg,
    bio: 'Developed the Forum and alert-dispatch modules within the Spring Boot monolith, backed entirely by MySQL. Built direct, synchronous email and SMS dispatch via JavaMailSender and Twilio for OTP verification, SOS alerts, gate entries, and payment receipts — delivered straight from the request thread, with no separate queue or notification store to manage.',
    email: 'shreyajangid12@gmail.com',
    linkedin: 'https://www.linkedin.com/in/shreyajangid/',
    github: 'https://github.com/shreyajangid12',
  },
  {
    id: 3,
    name: 'Sneha Raja Ghongade',
    role: 'Full Stack Developer',
    photo: snehaImg,
    bio: 'Delivered the Analytics and Dashboard modules end-to-end, aggregating data directly across the monolith\u2019s services in a single composed response, no inter-service network calls required. Built the corresponding React 18 dashboard views with live charts, keeping the backend aggregation and frontend visualization tightly integrated.',
    email: 'snehaghongade642@gmail.com',
    linkedin: 'https://www.linkedin.com/in/sneha-ghongade',
    github: 'https://github.com/snehaghongadeDev',
  },
  {
    id: 4,
    name: 'Varad Nishant Patil',
    role: 'Project Lead & Full Stack Developer',
    photo: varadImg,
    bio: 'Led the overall development and architecture of the Pravesh platform, coordinating the team and defining module boundaries, database design, and development standards within a single Spring Boot monolith. Implemented JWT-based authentication and role-based authorization (RESIDENT/GUARD/SOCIETY_ADMIN/SUPER_ADMIN), owned the overall MySQL schema design, and contributed across both backend and frontend development to deliver a secure, unified access control system.',
    email: 'varadpatil466@gmail.com',
    linkedin: 'https://www.linkedin.com/in/varad-nishant-patil-4159822b0',
    github: 'https://github.com/Varadpatil1812',
  },
  {
    id: 5,
    name: 'Vyankatesh Deepak Wakde',
    role: 'Backend Developer',
    photo: vyankiiImg,
    bio: 'Built the SOS and Payment modules in the Spring Boot monolith, implementing WebSocket STOMP for sub-second emergency alert push and Razorpay integration with webhook signature verification for maintenance payments. Ensured both modules followed the platform-wide standard for validation and exception handling.',
    email: 'vyankateshwakde23@gmail.com',
    linkedin: 'https://www.linkedin.com/in/vyankatesh-wakde-6b5a3334b',
    github: 'https://github.com/vyankateshwakdecmfeb26',
  },
];

export default function AboutPage() {
  useScrollReveal()

  return (
    <>
      <Navbar />
      <div className="about-page pv-page">

        {/* ── Dark banner ── */}
        <section className="pv-banner">
          <div className="pv-banner-aurora" aria-hidden="true"><span></span><span></span></div>
          <div className="pv-banner-noise" aria-hidden="true"></div>
          <div className="pv-banner-dots" aria-hidden="true"></div>
          <div className="pv-banner-inner">
            <div className="pv-pill"><span className="pv-pill-dot"></span>About Pravesh</div>
            <h1>Built by a team that<br /><span className="pv-glow">ships real software</span>.</h1>
            <p>
              Pravesh is an intelligent access control system for gated communities —
              replacing paper registers with a fast, fully digital pass-and-verification platform.
            </p>
          </div>
        </section>

        {/* ── Hero Info Card ── */}
        <div className="about-hero-card" data-reveal>
          <div className="about-hero-text">
            <p className="about-hero-lead">
              <i>Pravesh is an intelligent access control system built to secure gated communities.</i>
            </p>
            <p className="about-para">
              <i>Our mission is to replace paper visitor registers and manual gate checks with a fast,
                reliable, and fully digital pass-and-verification system.</i>
            </p>
            <p className="about-para">
              <i>Pravesh brings residents, guards, and society
                administrators onto one connected platform — from QR pass creation to real-time entry logs.</i>
            </p>
          </div>

          <div className="about-hero-brand">
            <img src={logoMark} alt="Pravesh" className="pravesh-brand-icon" />
          </div>
        </div>

        {/* ── Team Section ── */}
        <div className="about-inner">
          <div className="pv-sec-head" data-reveal>
            <span className="pv-eyebrow">Who built it</span>
            <h2 className="pv-sec-title">Meet The <span className="pv-grad">Team</span></h2>
          </div>

          <div className="card-section">
            {teamMembers.map(member => (
              <div className="info-card pv-card" key={member.id} data-reveal>
                <div className="pv-card-glow"></div>
                {member.photo ? (
                  <img src={member.photo} alt={member.name} className="member-photo" />
                ) : (
                  <DefaultAvatar name={member.name} />
                )}
                <h4 className="member-name">{member.name}</h4>
                <h4 className="member-role">( {member.role} )</h4>
                <p className="about-para">{member.bio}</p>
                <div className="social-links">
                  {member.email && (
                    <SocialIcon
                      href={`https://mail.google.com/mail/?view=cm&fs=1&to=${member.email}`}
                      icon="bi-envelope-fill"
                      title="Email"
                    />
                  )}
                  <SocialIcon href={member.linkedin} icon="bi-linkedin" title="LinkedIn" />
                  <SocialIcon href={member.github} icon="bi-github" title="GitHub" />
                </div>
              </div>
            ))}
          </div>

          {/* ── Mission Card ── */}
          <div className="card mission-card" data-reveal>
            <div className="card-body">
              <h2 className="mission-title">Our Mission</h2>
              <p className="mission-text">
                Our mission is to make residential gate security faster, more transparent, and easier to
                manage for everyone involved. We aim to give residents control over who can enter on their
                behalf, give guards a simple way to verify visitors in seconds, and give society admins a
                clear, searchable record of every entry — all without a single paper register.
              </p>
            </div>
          </div>

          <div className="text-center mt-4 mb-5" data-reveal>
            <Link to="/register" className="pv-btn-glow"><span>Join Pravesh</span><i className="bi bi-arrow-right"></i></Link>
          </div>
        </div>

      </div>
    </>
  )
}
