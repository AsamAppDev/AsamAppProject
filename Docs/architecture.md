flowchart TD

A[👤 کاربر سالمند] -->|WebView| B[📱 اپلیکیشن آسام‌اَپ]
B --> C[🌐 سایت وردپرس (AsamApp.ir)]
C --> D[⚙️ افزونه‌های سفارشی (Chatbot, Request, etc)]
D --> E[🗄️ API لایه کاربردی]
E --> F[(💾 پایگاه داده MySQL)]
E --> G[📂 فایل‌های رسانه‌ای / بکاپ]
C --> H[🎨 رابط کاربری المنتور]
B --> I[🔔 اعلان‌ها و نوتیفیکیشن‌ها]

style A fill:#f6f6f6,stroke:#333,stroke-width:1px
style B fill:#dce6ff,stroke:#333,stroke-width:1px
style C fill:#fff3cd,stroke:#333,stroke-width:1px
style D fill:#ffe0e0,stroke:#333,stroke-width:1px
style E fill:#e0ffe0,stroke:#333,stroke-width:1px
style F fill:#f2f2f2,stroke:#333,stroke-width:1px
