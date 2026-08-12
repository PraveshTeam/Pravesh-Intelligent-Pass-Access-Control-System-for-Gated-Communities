import { useEffect, useRef, useState } from 'react'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

// VITE_WS_BASE_URL is set at build time -- on AWS this is your EC2 public
// IP/domain on port 8084 (notification-service is deliberately connected to
// directly, bypassing the gateway, due to the Spring Cloud Gateway
// WebSocket-over-lb:// routing quirk documented in the hook below).
const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'http://localhost:8080'

export function useSosSocket(societyId, onAlert) {
  const [connected, setConnected] = useState(false)
  const clientRef = useRef(null)

  const onAlertRef = useRef(onAlert)
  useEffect(() => {
    onAlertRef.current = onAlert
  }, [onAlert])

  useEffect(() => {
    if (!societyId) return

    const client = new Client({
      webSocketFactory: () => new SockJS(`${WS_BASE_URL}/ws`),
      reconnectDelay: 4000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    })

    client.onConnect = () => {
      setConnected(true)
      client.subscribe(`/topic/sos/${societyId}`, (message) => {
        try {
          onAlertRef.current(JSON.parse(message.body))
        } catch {
          // ignore malformed frames
        }
      })
    }

    client.onDisconnect = () => setConnected(false)
    client.onStompError = () => setConnected(false)

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
    }
  }, [societyId])
}
