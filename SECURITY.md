# Security Notice

This project is a **demo banking simulator**. Authentication and
authorization are intentionally omitted so that any frontend client can
exercise the full API without a token flow.

DO NOT deploy this to a public environment as-is.

Planned hardening (see `AccountController` TODOs):
- Spring Security + JWT bearer tokens
- `@PreAuthorize("hasRole('USER')")` on all endpoints
- Ownership check: caller can only access their own accountId
- Rate limiting on transaction endpoints
- Audit log for every state-changing call