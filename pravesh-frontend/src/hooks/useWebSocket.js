import { useEffect, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

/*
 * Connects to the notification-service STOMP endpoint and subscribes to
 * /topic/flat/{flatId}/notifications.
 *
 * VITE_WS_BASE_URL set at build time -- on AWS this is your EC2 public
 * IP/domain, port 8084 (same direct-to-notification-service pattern as
 * useSosSocket/useSosStatusSocket, for the same gateway-routing reason).
 */
const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'http://localhost:8080'

export function useWebSocket(flatId, onMessage) {
  const clientRef = useRef(null)

  useEffect(() => {
    if (!flatId) return

    const client = new Client({
      webSocketFactory: () => new SockJS(`${WS_BASE_URL}/ws`),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/flat/${flatId}/notifications`, (frame) => {
          try {
            onMessage(JSON.parse(frame.body))
          } catch {
            onMessage({ message: frame.body })
          }
        })
      },
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
      clientRef.current = null
    }
  }, [flatId, onMessage])

  return clientRef
}
