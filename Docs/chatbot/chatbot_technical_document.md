
### **مستندات فنی پروژه: هسته اصلی چت‌بات (FastAPI Backend)**

این مستندات به منظور ارائه یک راهنمای فنی عمیق برای توسعه‌دهندگان تهیه شده است تا با معماری، منطق و جزئیات پیاده‌سازی سرویس بک‌اند چت‌بات آشنا شوند.

#### **۱. نمای کلی پروژه**

**هدف پروژه:**
این سرویس بک‌اند که با استفاده از فریمورک FastAPI در پایتون توسعه داده شده، به عنوان مغز متفکر چت‌بات عمل می‌کند. وظیفه اصلی آن دریافت درخواست‌ها از کلاینت (مانند افزونه وردپرس)، پردازش آن‌ها با استفاده از مدل‌های زبانی بزرگ (LLM) از طریق کتابخانه LangChain و مدیریت تاریخچه مکالمات در یک پایگاه داده PostgreSQL است.

**ویژگی‌های اصلی:**
*   **API مبتنی بر FastAPI:** ارائه نقاط پایانی (Endpoints) سریع و مدرن برای تعامل با چت‌بات.
*   **یکپارچه‌سازی با LangChain:** استفاده از قدرت کتابخانه LangChain برای ساخت زنجیره‌های پردازش زبان، مدیریت تاریخچه و تعامل با مدل‌های زبان.
*   **مدیریت تاریخچه مکالمات:** ذخیره و بازیابی خودکار تاریخچه هر مکالمه در پایگاه داده PostgreSQL بر اساس یک شناسه جلسه (`session_id`).
*   **مدیریت جلسات کاربر:** قابلیت ذخیره متادیتای جلسات (مانند شناسه کاربر وردپرس و عنوان چت) و بازیابی آن‌ها.
*   **پیکربندی مبتنی بر environment:** مدیریت آسان تنظیمات (مانند کلیدهای API و اطلاعات اتصال به پایگاه داده) از طریق فایل .env.

**پشته فناوری (Technology Stack):**
*   **فریمورک وب:** FastAPI
*   **سرور ASGI:** Uvicorn
*   **پردازش های هوش مصنوعی:** LangChain, LangChain-OpenAI
*   **پایگاه داده:** PostgreSQL
*   **ارتباط با پایگاه داده:** `psycopg` (برای اتصالات مستقیم و LangChain)، SQLAlchemy (برای CRUD عملیات مربوط به جلسات)
*   **مدیریت تنظیمات:** Pydantic

---

#### **۲. راه‌اندازی محیط توسعه**

**پیش‌نیازها:**
*   نصب پایتون (نسخه ۳.۱۲ یا بالاتر).
*   نصب و راه‌اندازی یک سرور PostgreSQL.

**مراحل نصب و راه‌اندازی:**
1.  **نصب وابستگی‌ها:** در ریشه پروژه، دستور زیر را برای نصب تمام کتابخانه‌های مورد نیاز اجرا کنید:
    ```bash
    pip install -r requirements.txt
    ```

2.  **پیکربندی متغیرهای محیطی:**
    یک فایل با نام .env در ریشه پروژه ایجاد کرده و مقادیر زیر را مطابق با تنظیمات خود در آن قرار دهید. این مقادیر توسط برنامه برای اتصال به سرویس‌های خارجی استفاده می‌شوند.
    ````
    // filepath: \root\projects\chatbot\.env
    # آدرس پایه و کلید API برای مدل زبان
    OPENAI_API_BASE=https://api.avalai.ir/v1/
    OPENAI_API_KEY=YOUR_API_KEY_HERE

    # اطلاعات اتصال به پایگاه داده PostgreSQL
    DATABASE_URL="postgresql://USERNAME:PASSWORD@HOST:PORT/DATABASE_NAME"
    DATABASE_TABLE="chat_history" # نام جدول برای تاریخچه پیام‌های LangChain
    ````

3.  **اجرای برنامه:**
    برای اجرای سرور توسعه، از دستور زیر در ریشه پروژه استفاده کنید:
    ```bash
    uvicorn src.main:app --host 127.0.0.1 --port 8000
    ```
    این دستور برنامه را با استفاده از Uvicorn اجرا می کند. سرور به طور پیش‌فرض روی آدرس `http://127.0.0.1:8000` در دسترس خواهد بود.

---

#### **۳. تحلیل اجزا و عملکرد کد**

در این بخش، هر فایل اصلی پروژه به تفصیل بررسی می‌شود.

**۱. نقطه ورود برنامه (main.py)**

این فایل مسئول راه‌اندازی اولیه برنامه، اتصال به پایگاه داده و تعریف مسیرهای اصلی است.

*   **عملکرد کلی:**
    *   بارگذاری متغیرهای محیطی از فایل .env.
    *   بررسی اتصال به پایگاه داده و ایجاد جداول `chat_history` (توسط LangChain) و `chat_sessions` (به صورت دستی) در صورت عدم وجود.
    *   ایجاد یک نمونه از `FastAPI` و پیکربندی CORS برای اجازه دادن به درخواست‌ها از دامنه‌های دیگر (مانند سایت وردپرس).
    *   افزودن روتر (Router) تعریف شده در endpoints.py به برنامه اصلی.

*   **توضیح کد:**
    ```python
    # filepath: \root\projects\chatbot\src\main.py
    # ...existing code...
    try:
        sync_connection = psycopg.connect(settings.DATABASE_URL)
        # ایجاد جدول تاریخچه توسط LangChain
        PostgresChatMessageHistory.create_tables(sync_connection, settings.DATABASE_TABLE)

        # ایجاد جدول جلسات به صورت دستی
        with sync_connection.cursor() as cursor:
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS chat_sessions ( ... );
                CREATE INDEX IF NOT EXISTS ... ;
            """)
            sync_connection.commit()
        # ...existing code...
    except Exception as e:
        log.error(f"Database connection failed: {e}")

    app = FastAPI(...)

    # پیکربندی CORS
    app.add_middleware(
        CORSMiddleware,
        allow_origins=origins,
        # ...
    )

    # افزودن مسیرهای API
    app.include_router(endpoints.router, prefix="/api")
    # ...existing code...
    ```
    در ابتدای اجرا، برنامه تلاش می‌کند به پایگاه داده متصل شود. سپس با استفاده از متد استاتیک `PostgresChatMessageHistory.create_tables` جدولی را که LangChain برای ذخیره پیام‌ها نیاز دارد، ایجاد می‌کند. بلافاصله پس از آن، جدول `chat_sessions` که برای ذخیره متادیتای هر جلسه چت (مانند `user_id` و `title`) استفاده می‌شود، با یک کوئری SQL خام ایجاد می‌گردد. در نهایت، روتر API با پیشوند `/api` به برنامه اضافه می‌شود، به این معنی که تمام مسیرهای تعریف شده در endpoints.py از طریق `/api/...` در دسترس خواهند بود.

**۲. مدیریت تنظیمات (config.py)**

این فایل با استفاده از Pydantic، متغیرهای محیطی را مدیریت و اعتبارسنجی می‌کند.

*   **عملکرد کلی:**
    *   تعریف یک کلاس `Settings` که از `BaseSettings` ارث‌بری می‌کند.
    *   تعریف فیلدها برای هر متغیر محیطی مورد نیاز (`OPENAI_API_KEY`, `DATABASE_URL` و غیره).
    *   Pydantic به طور خودکار مقادیر را از فایل .env خوانده و در نمونه‌ای از کلاس `Settings` قرار می‌دهد.

*   **توضیح کد:**
    ```python
    # filepath: \root\projects\chatbot\src\core\config.py
    from pydantic_settings import BaseSettings, SettingsConfigDict

    class Settings(BaseSettings):
        OPENAI_API_KEY: str
        OPENAI_API_BASE: str
        DATABASE_URL: str
        DATABASE_TABLE: str
        MODEL_NAME: str = "gpt-4o"
        
        model_config = SettingsConfigDict(env_file=".env")

    settings = Settings()
    ```
    با ایجاد یک نمونه از `Settings`، Pydantic به صورت خودکار فایل .env را پیدا کرده، مقادیر را می‌خواند و آن‌ها را به فیلدهای مربوطه در کلاس تخصیص می‌دهد. اگر متغیری وجود نداشته باشد، برنامه با خطا متوقف می‌شود. این روش، مدیریت تنظیمات را متمرکز و ایمن می‌سازد.

**۳. سرویس اصلی چت‌بات (service.py)**

این فایل قلب تپنده منطق چت‌بات است و مسئولیت تعامل با مدل زبانی را بر عهده دارد.

*   **عملکرد کلی:**
    *   تعریف کلاس `Chatbot` که زنجیره پردازش LangChain را در خود کپسوله می‌کند.
    *   در سازنده (`__init__`), یک قالب پرامپت (Prompt Template)، یک نمونه از مدل زبان (`ChatOpenAI`) و یک زنجیره با قابلیت مدیریت تاریخچه (`RunnableWithMessageHistory`) ایجاد می‌شود.
    *   متد `get_chat_response` پیام کاربر و شناسه جلسه را دریافت کرده و پاسخ مدل را برمی‌گرداند.

*   **توضیح کد:**
    ```python
    # filepath: \root\projects\chatbot\src\chatbot\service.py
    # ...existing code...
    class Chatbot:
        def __init__(self):
            # ...
            prompt_template = ChatPromptTemplate.from_messages([...])
            llm = ChatOpenAI(...)

            chain = prompt_template | llm

            self.chain_with_history = RunnableWithMessageHistory(
                chain,
                lambda session_id: PostgresChatMessageHistory(
                    settings.DATABASE_TABLE,
                    session_id,
                    sync_connection=self.sync_connection,
                ),
                input_messages_key="question",
                history_messages_key="history",
            )
        
        def get_chat_response(self, question: str, session_id: str) -> str:
            # ...
            config = {"configurable": {"session_id": session_id}}
            response = self.chain_with_history.invoke({"question": question}, config=config)
            # ...
            return response.content
    ```
    منطق اصلی در `RunnableWithMessageHistory` نهفته است. این کلاس یک زنجیره (`chain`) را به عنوان ورودی اول می‌گیرد. ورودی دوم یک تابع `lambda` است که به ازای هر `session_id`، یک نمونه از `PostgresChatMessageHistory` را برمی‌گرداند. این نمونه مسئول خواندن و نوشتن پیام‌ها در جدول `chat_history` پایگاه داده برای آن `session_id` خاص است.
    هنگامی که متد `get_chat_response` با `invoke` زنجیره را فراخوانی می‌کند، `RunnableWithMessageHistory` به طور خودکار:
    1.  تاریخچه پیام‌های مربوط به `session_id` داده شده را از پایگاه داده می‌خواند.
    2.  تاریخچه را به همراه پیام جدید کاربر (`question`) در پرامپت قرار می‌دهد.
    3.  پرامپت کامل را به مدل زبان (`llm`) ارسال می‌کند.
    4.  پیام جدید کاربر و پاسخ مدل را در پایگاه داده برای همان `session_id` ذخیره می‌کند.
    5.  پاسخ نهایی را برمی‌گرداند.

**۴. نقاط پایانی API (endpoints.py)**

این فایل مسیرهای HTTP را تعریف می‌کند که کلاینت‌ها می‌توانند با آن‌ها تعامل داشته باشند.

*   **عملکرد کلی:**
    *   تعریف یک `APIRouter` برای گروه‌بندی مسیرهای مرتبط.
    *   **مسیر `POST /chat`:** درخواست چت را دریافت می‌کند، در صورت لزوم یک رکورد جلسه جدید در جدول `chat_sessions` ایجاد می‌کند و برای دریافت پاسخ، سرویس چت‌بات را فراخوانی می‌کند.
    *   **مسیر `GET /sessions/{user_id}`:** تمام جلسات چت یک کاربر خاص را برمی‌گرداند.
    *   **مسیر `GET /history/{session_id}`:** تاریخچه کامل پیام‌های یک جلسه خاص را برمی‌گرداند.

*   **توضیح کد:**
    ```python
    # filepath: \root\projects\chatbot\src\api\endpoints.py
    # ...existing code...
    @router.post("/chat", response_model=schemas.ChatResponse)
    def chat(request: schemas.ChatRequest, db: Session = Depends(db_session.get_db)):
        # ... اعتبارسنجی ورودی ...
        
        # اگر جلسه جدید است، آن را در جدول chat_sessions ثبت کن
        if not crud.session_exists(db, request.session_id):
            title = request.question[:50] + "..."
            crud.create_user_session(db, request.session_id, request.user_id, title)

        # فراخوانی سرویس اصلی چت‌بات
        response_text = chatbot_instance.get_chat_response(
            question=request.question,
            session_id=request.session_id
        )
        return schemas.ChatResponse(answer=response_text)

    @router.get("/history/{session_id}", response_model=list[schemas.HistoryMessage])
    def get_session_history(session_id: str):
        # بازیابی مستقیم تاریخچه با استفاده از ابزار LangChain
        history = PostgresChatMessageHistory(
            settings.DATABASE_TABLE,
            session_id,
            sync_connection=sync_connection,
        )
        return [{"type": msg.type, "content": msg.content} for msg in history.messages]
    ```
    مسیر `/chat` به عنوان نقطه تعامل اصلی عمل می‌کند. این مسیر ابتدا بررسی می‌کند که آیا `session_id` ارسال شده قبلاً در جدول `chat_sessions` وجود دارد یا خیر. اگر وجود نداشته باشد (یعنی این اولین پیام یک مکالمه جدید است)، یک رکورد جدید با استفاده از توابع `crud` برای آن جلسه ایجاد می‌کند. سپس، متد `get_chat_response` از نمونه `chatbot_instance` را فراخوانی می‌کند تا پاسخ تولید شود.
    مسیر `/history/{session_id}` نشان می‌دهد که چگونه می‌توان به راحتی با استفاده از همان کلاس `PostgresChatMessageHistory` به تاریخچه کامل یک مکالمه دسترسی پیدا کرد.

**۵. مدل‌های داده و پایگاه داده (schemas.py, models.py, `crud.py`)**

*   **schemas.py:** این فایل مدل‌های Pydantic را برای اعتبارسنجی داده‌های ورودی و خروجی API تعریف می‌کند. `ChatRequest` ساختار درخواست کلاینت و `ChatResponse` ساختار پاسخ سرور را مشخص می‌کند. این کار تضمین می‌کند که داده‌های مبادله شده همیشه فرمت صحیحی دارند.
*   **models.py:** این فایل مدل SQLAlchemy (`ChatSession`) را برای جدول `chat_sessions` تعریف می‌کند. این مدل به ORM اجازه می‌دهد تا رکوردهای این جدول را به صورت اشیاء پایتون مدیریت کند.
*   **crud.py (فایل ضمنی):** این ماژول شامل توابع عملیاتی پایگاه داده (Create, Read, Update, Delete) برای مدل `ChatSession` است. توابعی مانند `create_user_session`, `get_sessions_by_user` و `session_exists` در این فایل قرار دارند و منطق تعامل با جدول `chat_sessions` را از منطق API جدا می‌کنند.