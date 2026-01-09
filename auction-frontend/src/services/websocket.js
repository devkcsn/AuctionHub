import SockJS from 'sockjs-client/dist/sockjs';
import { Client } from '@stomp/stompjs';

class WebSocketService {
  constructor() {
    this.client = null;
    this.subscriptions = new Map();
    this.connected = false;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 10;
    this.listeners = new Map();
  }

  connect(token) {
    if (this.client && this.connected) return;

    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      onConnect: () => {
        console.log('WebSocket connected');
        this.connected = true;
        this.reconnectAttempts = 0;

        // Re-subscribe to all previous subscriptions
        this.subscriptions.forEach((callback, destination) => {
          this._subscribe(destination, callback);
        });

        // Notify connection listeners
        this._notifyListeners('connect', { connected: true });
      },
      onDisconnect: () => {
        console.log('WebSocket disconnected');
        this.connected = false;
        this._notifyListeners('disconnect', { connected: false });
      },
      onStompError: (frame) => {
        console.error('WebSocket STOMP error:', frame.headers['message']);
        this._notifyListeners('error', { error: frame.headers['message'] });
      },
    });

    this.client.activate();
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.connected = false;
      this.subscriptions.clear();
    }
  }

  subscribeToAuction(auctionId, callback) {
    const destination = `/topic/auction/${auctionId}`;
    this.subscriptions.set(destination, callback);
    if (this.connected) {
      this._subscribe(destination, callback);
    }
    return () => this.unsubscribe(destination);
  }

  subscribeToAllAuctions(callback) {
    const destination = '/topic/auctions';
    this.subscriptions.set(destination, callback);
    if (this.connected) {
      this._subscribe(destination, callback);
    }
    return () => this.unsubscribe(destination);
  }

  subscribeToNotifications(username, callback) {
    const destination = `/user/${username}/queue/notifications`;
    this.subscriptions.set(destination, callback);
    if (this.connected) {
      this._subscribe(destination, callback);
    }
    return () => this.unsubscribe(destination);
  }

  sendBid(bidRequest) {
    if (this.connected && this.client) {
      this.client.publish({
        destination: '/app/bid',
        body: JSON.stringify(bidRequest),
      });
    }
  }

  unsubscribe(destination) {
    this.subscriptions.delete(destination);
  }

  addListener(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, []);
    }
    this.listeners.get(event).push(callback);
  }

  removeListener(event, callback) {
    if (this.listeners.has(event)) {
      const listeners = this.listeners.get(event).filter((l) => l !== callback);
      this.listeners.set(event, listeners);
    }
  }

  _subscribe(destination, callback) {
    if (this.client && this.connected) {
      this.client.subscribe(destination, (message) => {
        try {
          const data = JSON.parse(message.body);
          callback(data);
        } catch (e) {
          console.error('Error parsing WebSocket message:', e);
        }
      });
    }
  }

  _notifyListeners(event, data) {
    if (this.listeners.has(event)) {
      this.listeners.get(event).forEach((callback) => callback(data));
    }
  }

  isConnected() {
    return this.connected;
  }
}

const wsService = new WebSocketService();
export default wsService;
