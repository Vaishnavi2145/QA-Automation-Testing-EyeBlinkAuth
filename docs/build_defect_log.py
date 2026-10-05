import openpyxl
from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
from openpyxl.utils import get_column_letter

wb = openpyxl.Workbook()
ws = wb.active
ws.title = "Defect Log"

HEADER_FILL = PatternFill(start_color="7A1F1F", end_color="7A1F1F", fill_type="solid")
HEADER_FONT = Font(name="Arial", bold=True, color="FFFFFF", size=10)
BODY_FONT = Font(name="Arial", size=10)
WRAP = Alignment(wrap_text=True, vertical="top")
THIN = Side(style="thin", color="D9D9D9")
BORDER = Border(left=THIN, right=THIN, top=THIN, bottom=THIN)

SEV_COLORS = {
    "Critical": "C00000",
    "High": "E36C09",
    "Medium": "BF8F00",
    "Low": "808080",
}

headers = [
    "Defect ID", "Module", "Title", "Description", "Steps to Reproduce",
    "Expected", "Actual", "Severity", "Priority", "Status", "Linked Test Case"
]

rows = [
["BUG-01","Signup","/signup blocks the Flask server on a native OpenCV camera window",
 "app.py's /signup handler calls capture_face_samples(), which opens a blocking "
 "cv2.VideoCapture(0) + cv2.imshow()/cv2.waitKey() loop directly inside the request "
 "handler, waiting for a physical 's' keypress in a native window on whatever machine "
 "is running the Flask process.",
 "1. Run the app\n2. Submit a fully valid /signup form (name, email, numeric blink_count)\n"
 "3. Observe the server process",
 "Face capture should happen client-side (as it does on /login) so the request completes "
 "normally and the app can be deployed to a server with no physical webcam attached.",
 "The Flask worker thread blocks until someone at the server's own keyboard/monitor presses "
 "'s' 8 times or 'q'. In any real deployment (no local camera, no one watching the server), "
 "the request hangs indefinitely and ties up a worker.",
 "Critical","P1","Open","TC-SU-01"],

["BUG-02","Signup / Login","Inconsistent face-capture architecture between Signup and Login",
 "Signup captures the face server-side via OpenCV (see BUG-01). Login captures the face "
 "client-side in the browser (getUserMedia + canvas, sent as a base64 'face_image' field).",
 "Compare capture_face_samples() in app.py (used by /signup) with blink.js's browser-side "
 "capture (used by /login).",
 "Both flows should use the same capture mechanism for consistency and deployability.",
 "Two different, incompatible architectures for what should be the same capability - "
 "the signup path cannot work at all once deployed away from the developer's own machine.",
 "High","P1","Open","TC-SU-01, TC-LG-01"],

["BUG-03","File Management","IDOR - /download/<email>/<filename> has no authentication check",
 "Every other protected route (/dashboard, /files, /upload) checks "
 "`if \"user_email\" not in session` before proceeding. /download does not.",
 "1. With no login and no session cookie, send GET /download/<any_known_email>/<any_known_filename>\n"
 "2. Observe the response",
 "Should redirect to /login (401/302) like every other protected route, and should verify "
 "the requester's session email matches the folder being requested.",
 "The file is returned directly with HTTP 200 - any user's uploaded files can be downloaded "
 "by anyone who knows or guesses their email and a filename. No login required at all.",
 "Critical","P1","Open","TC-SEC-06"],

["BUG-04","Signup","No UNIQUE constraint on users.email in the live schema",
 "db/blinkauth.sql's CREATE TABLE for `users` does not declare email as UNIQUE, unlike the "
 "commented-out init_db() function in app.py which does. The signup handler still relies on "
 "catching an IntegrityError to show 'Email already exists.'",
 "1. Import db/blinkauth.sql as-is\n2. Sign up twice with the same email",
 "Second signup should be rejected with 'Email already exists. Try logging in.'",
 "UNVERIFIED without a live run, but per the schema as shipped, no IntegrityError will be "
 "raised, so the duplicate-email branch is likely unreachable and two rows with the same "
 "email can both be inserted.",
 "High","P1","Open","TC-SU-06"],

["BUG-05","Login","Security-notification email is non-functional (hardcoded placeholder credentials)",
 "sendemail() hardcodes 'your-email@gmail.com' / 'your-app-password-here' as SMTP "
 "credentials. It IS called on face mismatch and blink mismatch to notify the account owner "
 "of a failed access attempt.",
 "1. Trigger a face or blink mismatch on /login\n2. Check server logs",
 "An alert email should be sent to the account owner.",
 "smtplib.SMTP.login() will fail with the placeholder credentials; the exception is caught "
 "and only printed to the console. The account owner is never actually notified of a failed "
 "login attempt, silently defeating a security feature.",
 "Medium","P2","Open","TC-LG-06, TC-LG-07"],

["BUG-06","Signup / Login","No CSRF protection on Signup or Login forms",
 "Neither form includes a CSRF token, and Flask-WTF/CSRF protection is not configured in app.py.",
 "1. Inspect the rendered HTML of /signup and /login for a hidden csrf token field",
 "A per-session CSRF token should be present and validated server-side.",
 "No token is present or checked - both forms are vulnerable to Cross-Site Request Forgery.",
 "Medium","P2","Open","TC-SEC-01, TC-SEC-02"],

["BUG-07","Login","Unhandled FileNotFoundError if ref_embed.pkl doesn't exist yet",
 "login() does `with open(REF_EMBED_PATH, \"rb\") as f: embed_dict = pickle.load(f)` with no "
 "existence check and no try/except, unlike signup's more defensive pickle-loading pattern.",
 "1. On a fresh checkout/DB where no one has ever signed up (ref_embed.pkl absent)\n"
 "2. Attempt to log in with any email + blink_count + face_image",
 "Should show a graceful message, e.g. 'No face registered for this email.'",
 "Raises an unhandled FileNotFoundError, returning Flask's generic HTTP 500 page instead of "
 "a flash message.",
 "Medium","P2","Open","TC-SEC-08"],

["BUG-08","Login","Unhandled ValueError when face_image has no comma",
 "get_encoding_from_base64() does `header, encoded = image_data.split(\",\", 1)` with no "
 "guard for a string that contains no comma at all.",
 "1. POST to /login with face_image='not-a-data-url' (any value with no comma), a valid "
 "registered email, and a numeric blink_count",
 "Should show a graceful error, e.g. 'Face not detected properly.'",
 "Raises an unhandled ValueError ('not enough values to unpack'), returning HTTP 500.",
 "Medium","P2","Open","TC-SEC-07"],

["BUG-09","Login","Blink count is a client-trusted value, not a server-verified measurement",
 "The browser counts blinks via MediaPipe FaceMesh and sends only the final integer in a "
 "hidden field (#blinkInput). The server just compares two integers - it never verifies the "
 "submitted count came from real, live blink detection.",
 "1. Log in far enough to reach the camera step\n2. Edit the hidden #blinkInput value via "
 "browser dev tools to match the account's stored blink_count\n3. Submit without actually blinking",
 "The second factor should be tied to a live, server-verifiable signal, not a client-reported number.",
 "UNVERIFIED live (needs a real registered account to fully confirm), but by design the "
 "server-side check (`int(blink_count) != int(user['blink_count'])`) cannot distinguish a "
 "genuine blink sequence from a manually-edited field - the second factor can likely be "
 "spoofed by anyone with browser dev tools.",
 "High","P1","Open","TC-LG-08"],

["BUG-10","File Management","No file type, extension, or size validation on /upload",
 "The /upload handler saves request.files['file'] directly with its original filename and no "
 "checks on extension, MIME type, content, or size. Flask's MAX_CONTENT_LENGTH is not set.",
 "1. Log in\n2. Upload an executable, script, or a very large file",
 "Should enforce an allow-list of extensions/MIME types and a reasonable max file size.",
 "Any file type of any size (up to server/OS/disk limits) is accepted and stored as-is under "
 "static/uploads/<email>/ - a path that is also directly web-servable and unauthenticated "
 "(compounds BUG-03).",
 "Medium","P2","Open","TC-FILE-03, TC-FILE-04"],

["BUG-11","Configuration","Hardcoded Flask secret key and DB credentials in source",
 "app.secret_key is a hardcoded string literal, and dbconnection() hardcodes host/user/"
 "password rather than reading from environment variables or a config file.",
 "Read app.py lines defining `app.secret_key` and `dbconnection()`",
 "Secrets should come from environment variables / a secrets manager, never committed to source.",
 "A hardcoded, guessable secret key can allow forging signed session cookies; committing DB "
 "credentials to source is a standard security anti-pattern flagged in any code review.",
 "Medium","P2","Open","N/A - found via code review"],
]

ws.append(headers)
for col in range(1, len(headers) + 1):
    c = ws.cell(row=1, column=col)
    c.font = HEADER_FONT
    c.fill = HEADER_FILL
    c.alignment = Alignment(horizontal="center", vertical="center", wrap_text=True)
    c.border = BORDER
ws.row_dimensions[1].height = 24

sev_col_idx = headers.index("Severity") + 1

for r in rows:
    ws.append(r)

for row_idx in range(2, ws.max_row + 1):
    sev_val = ws.cell(row=row_idx, column=sev_col_idx).value
    for col_idx in range(1, len(headers) + 1):
        cell = ws.cell(row=row_idx, column=col_idx)
        cell.font = BODY_FONT
        cell.alignment = WRAP
        cell.border = BORDER
    sev_cell = ws.cell(row=row_idx, column=sev_col_idx)
    color = SEV_COLORS.get(sev_val)
    if color:
        sev_cell.font = Font(name="Arial", size=10, bold=True, color="FFFFFF")
        sev_cell.fill = PatternFill(start_color=color, end_color=color, fill_type="solid")

widths = [10, 16, 34, 46, 40, 34, 46, 10, 9, 10, 20]
for i, w in enumerate(widths, start=1):
    ws.column_dimensions[get_column_letter(i)].width = w

ws.freeze_panes = "A2"

# Summary sheet
summary = wb.create_sheet("Summary")
summary["A1"] = "Eye-Blink Authentication System - Defect Summary"
summary["A1"].font = Font(name="Arial", bold=True, size=13, color="7A1F1F")
summary.merge_cells("A1:D1")

from collections import Counter
sev_counts = Counter(r[7] for r in rows)
summary_rows = [
    ("Total defects logged", len(rows)),
    ("Critical", sev_counts.get("Critical", 0)),
    ("High", sev_counts.get("High", 0)),
    ("Medium", sev_counts.get("Medium", 0)),
    ("Low", sev_counts.get("Low", 0)),
]
for i, (label, val) in enumerate(summary_rows, start=3):
    summary.cell(row=i, column=1, value=label).font = Font(name="Arial", bold=True, size=10)
    summary.cell(row=i, column=2, value=val).font = BODY_FONT

summary.column_dimensions["A"].width = 24
summary.column_dimensions["B"].width = 10

note = summary.cell(row=len(summary_rows) + 5, column=1,
    value="All defects were identified through manual source-code review and, where marked "
          "'Automated', confirmed by an automated test in the QA suite (see TEST_CASES.xlsx "
          "'Linked Test Case' column). Entries marked UNVERIFIED in their Actual column still "
          "need a first live run against your local Flask + MySQL instance to confirm.")
note.font = Font(name="Arial", italic=True, size=9, color="666666")
note.alignment = Alignment(wrap_text=True)
summary.merge_cells(start_row=len(summary_rows) + 5, start_column=1, end_row=len(summary_rows) + 5, end_column=6)

wb.move_sheet("Summary", offset=-1)
wb.save("DEFECT_LOG.xlsx")
print("saved")
