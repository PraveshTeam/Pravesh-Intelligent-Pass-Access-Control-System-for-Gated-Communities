import { useEffect, useRef, useState } from 'react'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'http://localhost:8080'

/**
 * Subscribes to /topic/sos-status/{residentUserId} -- the resident's own
 * private live-status channel, separate from useSosSocket's society-wide
 * /topic/sos/{societyId}.
 */
export function useSosStatusSocket(residentUserId, onStatusUpdate) {
  const [connected, setConnected] = useState(false)
  const clientRef = useRef(null)

  const onStatusUpdateRef = useRef(onStatusUpdate)
  useEffect(() => {
    onStatusUpdateRef.current = onStatusUpdate
  }, [onStatusUpdate])

  useEffect(() => {
    if (!residentUserId) return

    const client = new Client({
      webSocketFactory: () => new SockJS(`${WS_BASE_URL}/ws`),
      reconnectDelay: 4000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    })

    client.onConnect = () => {
      setConnected(true)
      client.subscribe(`/topic/sos-status/${residentUserId}`, (message) => {
        try {
          const payload = JSON.parse(message.body)
          onStatusUpdateRef.current(payload)
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
  }, [residentUserId])

  return { connected }
}
