"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import PageHeader from "../../../components/ui/PageHeader";
import LoadingState from "../../../components/ui/LoadingState";
import ErrorState from "../../../components/ui/ErrorState";
import EmptyState from "../../../components/ui/EmptyState";
import { useAuth } from "../../../components/auth/AuthProvider";
import {
  createDirectThread,
  getDirectThreads,
  getProjectThreads,
  getTeamThreads,
  getThreadMessages,
  getUsers,
  sendThreadMessage
} from "../../../lib/api/workspace";
import type {
  MessageItem,
  MessageThreadDirect,
  MessageThreadProject,
  MessageThreadTeam,
  User
} from "../../../types/domain/workspace";

const CURRENT_USER_KEY = "phoenixtask.currentUserId";
const DEFAULT_USER_ID = 1;

type TabKey = "team" | "project" | "direct";

type ThreadItem = MessageThreadTeam | MessageThreadProject | MessageThreadDirect;

export default function MessagesPage() {
  const { can } = useAuth();
  const canRead = can("messages.read");
  const canSend = can("messages.send");
  const canCreateThread = can("messages.thread.create");
  const router = useRouter();
  const searchParams = useSearchParams();

  const [activeTab, setActiveTab] = useState<TabKey>("team");
  const [currentUserId, setCurrentUserId] = useState<number>(DEFAULT_USER_ID);
  const [users, setUsers] = useState<User[]>([]);
  const [threads, setThreads] = useState<ThreadItem[]>([]);
  const [messages, setMessages] = useState<MessageItem[]>([]);
  const [selectedThreadId, setSelectedThreadId] = useState<number | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "conversation">("list");
  const [loadingThreads, setLoadingThreads] = useState<boolean>(true);
  const [loadingMessages, setLoadingMessages] = useState<boolean>(false);
  const [threadError, setThreadError] = useState<string | null>(null);
  const [messageError, setMessageError] = useState<string | null>(null);
  const [composerText, setComposerText] = useState<string>("");
  const [sending, setSending] = useState<boolean>(false);
  const [dmTargetId, setDmTargetId] = useState<number | null>(null);
  const [dmStatus, setDmStatus] = useState<string | null>(null);
  const [reloadThreadsToken, setReloadThreadsToken] = useState<number>(0);
  const [reloadMessagesToken, setReloadMessagesToken] = useState<number>(0);

  const requestedThreadId = useMemo(() => {
    const value = searchParams.get("threadId");
    if (!value) return null;
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
  }, [searchParams]);

  useEffect(() => {
    const stored = typeof window !== "undefined" ? window.localStorage.getItem(CURRENT_USER_KEY) : null;
    if (stored) {
      const parsed = Number(stored);
      if (Number.isFinite(parsed) && parsed > 0) {
        setCurrentUserId(parsed);
        return;
      }
    }
    if (typeof window !== "undefined") {
      window.localStorage.setItem(CURRENT_USER_KEY, String(DEFAULT_USER_ID));
    }
    setCurrentUserId(DEFAULT_USER_ID);
  }, [canRead]);

  useEffect(() => {
    if (typeof window !== "undefined") {
      window.localStorage.setItem(CURRENT_USER_KEY, String(currentUserId));
    }
  }, [currentUserId]);

  useEffect(() => {
    if (!canRead) {
      setLoadingThreads(false);
      setThreadError(null);
      setThreads([]);
      return;
    }
    const loadUsers = async () => {
      try {
        const data = await getUsers();
        setUsers(data);
      } catch {
        setUsers([]);
      }
    };
    loadUsers();
  }, []);

  useEffect(() => {
    if (!canRead) {
      return;
    }
    if (users.length === 0) {
      return;
    }
    const valid = users.some((user) => user.id === currentUserId);
    if (!valid) {
      setCurrentUserId(DEFAULT_USER_ID);
      if (typeof window !== "undefined") {
        window.localStorage.setItem(CURRENT_USER_KEY, String(DEFAULT_USER_ID));
      }
    }
  }, [users, currentUserId, canRead]);

  useEffect(() => {
    if (!canRead) {
      setLoadingThreads(false);
      setThreadError(null);
      setThreads([]);
      return;
    }
    let cancelled = false;
    const loadThreads = async () => {
      setLoadingThreads(true);
      setThreadError(null);
      try {
        let data: ThreadItem[] = [];
        if (activeTab === "team") {
          data = await getTeamThreads(currentUserId);
        } else if (activeTab === "project") {
          data = await getProjectThreads(currentUserId);
        } else {
          data = await getDirectThreads(currentUserId);
        }
        if (!cancelled) {
          setThreads(data);
        }
      } catch (error) {
        if (!cancelled) {
          setThreadError(error instanceof Error ? error.message : "Unable to load threads");
          setThreads([]);
        }
      } finally {
        if (!cancelled) {
          setLoadingThreads(false);
        }
      }
    };
    if (currentUserId) {
      loadThreads();
    }
    return () => {
      cancelled = true;
    };
  }, [activeTab, currentUserId, reloadThreadsToken, canRead]);

  useEffect(() => {
    setViewMode("list");
  }, [activeTab]);

  useEffect(() => {
    if (!canRead) {
      return;
    }
    if (threads.length === 0) {
      setSelectedThreadId(null);
      setMessages([]);
      setViewMode("list");
      return;
    }
    const matchesRequested = requestedThreadId
      ? threads.some((thread) => thread.threadId === requestedThreadId)
      : false;
    const nextId = matchesRequested ? requestedThreadId : threads[0].threadId;
    if (nextId && nextId !== selectedThreadId) {
      setSelectedThreadId(nextId ?? null);
      router.replace(`/messages?threadId=${nextId}`);
      setViewMode("conversation");
    }
  }, [threads, requestedThreadId, selectedThreadId, router, canRead]);

  useEffect(() => {
    if (!canRead) {
      setLoadingMessages(false);
      setMessageError(null);
      setMessages([]);
      return;
    }
    let cancelled = false;
    const loadMessages = async () => {
      if (!selectedThreadId) {
        setMessages([]);
        return;
      }
      setLoadingMessages(true);
      setMessageError(null);
      try {
        const data = await getThreadMessages(selectedThreadId, currentUserId);
        if (!cancelled) {
          setMessages(data);
        }
      } catch (error) {
        if (!cancelled) {
          setMessageError(error instanceof Error ? error.message : "Unable to load messages");
          setMessages([]);
        }
      } finally {
        if (!cancelled) {
          setLoadingMessages(false);
        }
      }
    };
    if (currentUserId) {
      loadMessages();
    }
    return () => {
      cancelled = true;
    };
  }, [selectedThreadId, currentUserId, reloadMessagesToken, canRead]);

  const currentUser = users.find((user) => user.id === currentUserId) ?? null;

  const otherUsers = users.filter((user) => user.id !== currentUserId);

  const activeThread = threads.find((thread) => thread.threadId === selectedThreadId) ?? null;

  const handleSelectThread = (threadId: number) => {
    setSelectedThreadId(threadId);
    router.replace(`/messages?threadId=${threadId}`);
    setViewMode("conversation");
  };

  const handleSendMessage = async () => {
    if (!canSend) {
      setMessageError("You do not have permission to perform this action.");
      return;
    }
    if (!selectedThreadId || sending) return;
    const text = composerText.trim();
    if (!text) {
      setMessageError("Please correct the highlighted fields.");
      return;
    }
    setSending(true);
    setMessageError(null);
    try {
      await sendThreadMessage(selectedThreadId, currentUserId, text);
      setComposerText("");
      const refreshedMessages = await getThreadMessages(selectedThreadId, currentUserId);
      setMessages(refreshedMessages);
      const refreshedThreads = activeTab === "team"
        ? await getTeamThreads(currentUserId)
        : activeTab === "project"
          ? await getProjectThreads(currentUserId)
          : await getDirectThreads(currentUserId);
      setThreads(refreshedThreads);
    } catch (error) {
      setMessageError("Please correct the highlighted fields.");
    } finally {
      setSending(false);
    }
  };

  const handleCreateDirect = async () => {
    if (!canCreateThread) {
      setDmStatus("You do not have permission to perform this action.");
      return;
    }
    if (!dmTargetId || sending) return;
    setDmStatus(null);
    setSending(true);
    try {
      const thread = await createDirectThread(currentUserId, dmTargetId);
      setActiveTab("direct");
      setViewMode("conversation");
      const refreshedThreads = await getDirectThreads(currentUserId);
      setThreads(refreshedThreads);
      setSelectedThreadId(thread.threadId);
      router.replace(`/messages?threadId=${thread.threadId}`);
      setDmTargetId(null);
      setDmStatus("Saved successfully.");
    } catch (error) {
      setDmStatus("Please correct the highlighted fields.");
    } finally {
      setSending(false);
    }
  };

  const formatTimestamp = (value: string | null) => {
    if (!value) return "";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
  };

  if (!canRead) {
    return (
      <div className="page">
        <PageHeader title="Messages" subtitle="Team, project, and direct communication." />
        <EmptyState title="Access restricted" message="You do not have permission to view messages." />
      </div>
    );
  }

  return (
    <div className="messages-page">
      <PageHeader
        title="Messages"
        subtitle="Team, project, and direct conversations connected to your workspace."
      />

      <section className="messages-controls">
        <div className="tab-group">
          <button
            type="button"
            className={activeTab === "team" ? "tab is-active" : "tab"}
            onClick={() => setActiveTab("team")}
          >
            Team Messages
          </button>
          <button
            type="button"
            className={activeTab === "project" ? "tab is-active" : "tab"}
            onClick={() => setActiveTab("project")}
          >
            Project Messages
          </button>
          <button
            type="button"
            className={activeTab === "direct" ? "tab is-active" : "tab"}
            onClick={() => setActiveTab("direct")}
          >
            Direct Messages
          </button>
        </div>
        <div className="current-user">
          <label>
            Current user
            <select
              value={currentUserId}
              onChange={(event) => setCurrentUserId(Number(event.target.value))}
            >
              {users.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.firstName} {user.lastName}
                </option>
              ))}
            </select>
          </label>
        </div>
      </section>

      <section className="messages-layout" data-view={viewMode}>
        <div className="messages-sidebar">
          {activeTab === "direct" ? (
            <div className="dm-create">
              <label>
                Start direct message
                <select
                  value={dmTargetId ?? ""}
                  onChange={(event) => {
                    const value = event.target.value;
                    setDmTargetId(value ? Number(value) : null);
                  }}
                  disabled={!canCreateThread}
                >
                  <option value="">Select user</option>
                  {otherUsers.map((user) => (
                    <option key={user.id} value={user.id}>
                      {user.firstName} {user.lastName}
                    </option>
                  ))}
                </select>
              </label>
              <button type="button" onClick={handleCreateDirect} disabled={!canCreateThread || !dmTargetId || sending}>
                Open DM
              </button>
              {dmStatus ? (
                <p className={`form-message ${dmStatus === "Saved successfully." ? "success" : "error"}`}>
                  {dmStatus}
                </p>
              ) : null}
            </div>
          ) : null}

          {loadingThreads ? (
            <LoadingState />
          ) : threadError ? (
            <ErrorState onRetry={() => setReloadThreadsToken((value) => value + 1)} />
          ) : threads.length === 0 ? (
            <EmptyState size="compact" />
          ) : (
            <ul className="thread-list">
              {threads.map((thread) => {
                const isActive = thread.threadId === selectedThreadId;
                const label = thread.threadType === "TEAM"
                  ? (thread as MessageThreadTeam).teamName
                  : thread.threadType === "PROJECT"
                    ? `${(thread as MessageThreadProject).projectKey} · ${(thread as MessageThreadProject).projectName}`
                    : (thread as MessageThreadDirect).otherUserFullName;
                const subLabel = thread.threadType === "DIRECT"
                  ? (thread as MessageThreadDirect).otherUserEmail
                  : thread.threadType === "TEAM"
                    ? "Team thread"
                    : "Project thread";
                return (
                  <li key={thread.threadId}>
                    <button
                      type="button"
                      className={isActive ? "thread-button is-active" : "thread-button"}
                      onClick={() => handleSelectThread(thread.threadId)}
                    >
                      <div>
                        <strong>{label}</strong>
                        <span>{subLabel}</span>
                      </div>
                      <time>{formatTimestamp(thread.lastMessageAt)}</time>
                    </button>
                  </li>
                );
              })}
            </ul>
          )}
        </div>

        <div className="messages-panel">
          {activeThread ? (
            <div className="thread-header">
              <button
                type="button"
                className="thread-back mobile-only"
                onClick={() => setViewMode("list")}
              >
                Back to threads
              </button>
              <div>
                <h3>
                  {activeThread.threadType === "TEAM"
                    ? (activeThread as MessageThreadTeam).teamName
                    : activeThread.threadType === "PROJECT"
                      ? `${(activeThread as MessageThreadProject).projectKey} · ${(activeThread as MessageThreadProject).projectName}`
                      : (activeThread as MessageThreadDirect).otherUserFullName}
                </h3>
                <p>
                  {activeThread.threadType === "DIRECT"
                    ? (activeThread as MessageThreadDirect).otherUserEmail
                    : activeThread.threadType === "TEAM"
                      ? "Team conversation"
                      : "Project conversation"}
                </p>
              </div>
              <div className="thread-meta">
                <span>Last activity</span>
                <strong>{formatTimestamp(activeThread.lastMessageAt) || "No messages yet"}</strong>
              </div>
            </div>
          ) : null}

          {loadingMessages ? (
            <LoadingState />
          ) : messageError ? (
            <ErrorState onRetry={() => setReloadMessagesToken((value) => value + 1)} />
          ) : (
            <div className="message-stream">
              {messages.length === 0 ? (
                <EmptyState size="compact" />
              ) : (
                messages.map((message) => (
                  <div
                    key={message.messageId}
                    className={message.authorUserId === currentUserId ? "message message--own" : "message"}
                  >
                    <div className="message-meta">
                      <strong>{message.authorFullName}</strong>
                      <span>{formatTimestamp(message.createdAt)}</span>
                    </div>
                    <p>{message.body}</p>
                  </div>
                ))
              )}
            </div>
          )}

          {activeThread ? (
            <div className="message-composer">
              <textarea
                rows={3}
                placeholder={currentUser ? `Message as ${currentUser.firstName} ${currentUser.lastName}` : "Write a message"}
                value={composerText}
                onChange={(event) => setComposerText(event.target.value)}
                disabled={!canSend}
              />
              <div className="composer-actions">
                <span className="helper-text">Max 2000 characters</span>
                <button type="button" onClick={handleSendMessage} disabled={!canSend || sending}>
                  Send message
                </button>
              </div>
              {messageError ? <p className="form-message error">{messageError}</p> : null}
            </div>
          ) : (
            <EmptyState title="Select an item" message="Choose a record to see details." />
          )}
        </div>
      </section>
    </div>
  );
}
