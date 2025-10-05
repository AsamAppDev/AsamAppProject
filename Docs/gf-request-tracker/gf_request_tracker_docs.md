# مستندات فنی پروژه: افزونه پیگیری درخواست‌های Gravity Forms

این مستندات راهنمای فنی جامعی برای توسعه‌دهندگان است تا با معماری، ساختار و جزئیات پیاده‌سازی افزونه سفارشی پیگیری درخواست‌ها آشنا شوند.

---

## ۱. نمای کلی پروژه

### هدف پروژه
این افزونه یک سیستم مدیریت و پیگیری درخواست‌های پیشرفته را به Gravity Forms اضافه می‌کند. به جای ساخت یک سیستم کامل از صفر، از قدرت فرم‌سازی Gravity Forms استفاده کرده و فقط بخش مدیریت سفارشات، پیگیری وضعیت و اتصال به ارائه‌دهندگان را پیاده‌سازی می‌کند.

### ویژگی‌های اصلی

#### **برای کاربران:**
- مشاهده لیست درخواست‌های فعال و تاریخچه
- پیگیری وضعیت لحظه‌ای هر درخواست
- دریافت اعلان‌های ایمیلی برای تغییر وضعیت
- رابط کاربری ریسپانسیو و زیبا

#### **برای مدیران:**
- داشبورد مدیریتی با آمار کلی
- تغییر سریع وضعیت درخواست‌ها
- فیلتر و جستجوی پیشرفته
- سیستم رنگ‌بندی وضعیت‌ها

#### **برای ارائه‌دهندگان (مرحله بعدی):**
- API مرکزی برای دریافت درخواست‌ها
- پنل جداگانه برای مدیریت سفارشات
- نوتیفیکیشن‌های Real-time

### پشته فناوری

**Backend:**
- PHP 7.4+ (منطق افزونه)
- WordPress REST API
- MySQL/MariaDB (ذخیره‌سازی)

**Frontend:**
- Vanilla JavaScript
- CSS3 (طراحی ریسپانسیو)
- AJAX (ارتباط غیرهمزمان)

**وابستگی‌ها:**
- Gravity Forms (الزامی)
- WordPress 5.8+

---

## ۲. راه‌اندازی و نصب

### پیش‌نیازها
```bash
- WordPress 5.8 یا بالاتر
- PHP 7.4 یا بالاتر
- Gravity Forms نصب و فعال شده
- MySQL 5.6 یا بالاتر
```

### مراحل نصب

1. **دانلود و قرارگیری فایل‌ها:**
```bash
wp-content/plugins/gf-request-tracker/
```

2. **فعال‌سازی افزونه:**
   - پنل مدیریت → افزونه‌ها → افزونه‌های نصب شده
   - "پیگیری درخواست‌های GF" را فعال کنید

3. **ایجاد جداول دیتابیس:**
   - افزونه به صورت خودکار جداول مورد نیاز را ایجاد می‌کند
   - بررسی در phpMyAdmin: جدول `wp_gf_request_tracker`

### نحوه استفاده

#### برای کاربران:
```php
// کد کوتاه صفحه پیگیری
[gf_request_tracker_page]
```

#### برای مدیران:
- منوی جدید "پیگیری درخواست‌ها" در پنل ادمین

---

## ۳. معماری و ساختار پروژه

### ساختار فایل‌ها

```
gf-request-tracker/
│
├── gf-request-tracker.php          # فایل اصلی افزونه
├── includes/
│   ├── class-database.php          # مدیریت دیتابیس
│   ├── class-request-status.php    # مدیریت وضعیت‌ها
│   ├── class-frontend.php          # رابط کاربری
│   ├── class-admin.php             # پنل مدیریت
│   ├── class-ajax.php              # درخواست‌های AJAX
│   └── class-notifications.php     # سیستم اعلان‌ها
├── assets/
│   ├── css/
│   │   └── frontend.css
│   └── js/
│       └── frontend.js
└── README.md
```

### جریان داده (Data Flow)

```
[Gravity Form Entry] 
    ↓
[Hook: gform_after_submission]
    ↓
[GF_Request_Tracker_Database::save_request()]
    ↓
[wp_gf_request_tracker table]
    ↓
[Frontend Display / Admin Panel]
```

---

## ۴. تحلیل کد و اجزا

### فایل اصلی (gf-request-tracker.php)

**وظایف اصلی:**
- تعریف ثابت‌های افزونه
- بارگذاری کلاس‌ها
- راه‌اندازی هوک‌های وردپرس

```php
/**
 * فعال‌سازی افزونه
 */
function gf_request_tracker_activate() {
    GF_Request_Tracker_Database::create_table();
    flush_rewrite_rules();
}
register_activation_hook(__FILE__, 'gf_request_tracker_activate');

/**
 * بارگذاری کلاس‌ها
 */
function gf_request_tracker_init() {
    require_once plugin_dir_path(__FILE__) . 'includes/class-database.php';
    require_once plugin_dir_path(__FILE__) . 'includes/class-request-status.php';
    // ... سایر کلاس‌ها
    
    new GF_Request_Tracker_Frontend();
    new GF_Request_Tracker_Admin();
    new GF_Request_Tracker_AJAX();
    new GF_Request_Tracker_Notifications();
}
add_action('plugins_loaded', 'gf_request_tracker_init');
```

---

### کلاس Database (class-database.php)

**مسئولیت:** مدیریت تمام عملیات پایگاه داده

#### ساختار جدول:
```sql
CREATE TABLE wp_gf_request_tracker (
    id BIGINT(20) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    entry_id BIGINT(20) UNSIGNED NOT NULL,
    form_id INT(11) NOT NULL,
    user_id BIGINT(20) UNSIGNED,
    user_email VARCHAR(255),
    status VARCHAR(50) DEFAULT 'pending',
    assigned_to BIGINT(20) UNSIGNED,
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_entry (entry_id)
);
```

#### متدهای کلیدی:

```php
class GF_Request_Tracker_Database {
    
    /**
     * ذخیره درخواست جدید
     */
    public static function save_request($entry, $form) {
        global $wpdb;
        
        $user_id = get_current_user_id();
        $user_email = '';
        
        // استخراج ایمیل از فرم یا کاربر
        if ($user_id) {
            $user = get_userdata($user_id);
            $user_email = $user->user_email;
        } else {
            // جستجوی فیلد ایمیل در فرم
            foreach ($form['fields'] as $field) {
                if ($field->type === 'email') {
                    $user_email = rgar($entry, $field->id);
                    break;
                }
            }
        }
        
        $data = array(
            'entry_id'   => $entry['id'],
            'form_id'    => $form['id'],
            'user_id'    => $user_id ?: NULL,
            'user_email' => $user_email,
            'status'     => 'pending',
            'created_at' => current_time('mysql'),
            'updated_at' => current_time('mysql')
        );
        
        $wpdb->insert(
            $wpdb->prefix . 'gf_request_tracker',
            $data,
            array('%d', '%d', '%d', '%s', '%s', '%s', '%s')
        );
        
        return $wpdb->insert_id;
    }
    
    /**
     * دریافت درخواست‌های کاربر
     */
    public static function get_user_requests($user_id, $status = null) {
        global $wpdb;
        
        $where = $wpdb->prepare("user_id = %d", $user_id);
        
        if ($status) {
            $where .= $wpdb->prepare(" AND status = %s", $status);
        }
        
        return $wpdb->get_results(
            "SELECT * FROM {$wpdb->prefix}gf_request_tracker 
             WHERE {$where} 
             ORDER BY created_at DESC"
        );
    }
    
    /**
     * به‌روزرسانی وضعیت
     */
    public static function update_status($request_id, $new_status) {
        global $wpdb;
        
        $updated = $wpdb->update(
            $wpdb->prefix . 'gf_request_tracker',
            array(
                'status'     => $new_status,
                'updated_at' => current_time('mysql')
            ),
            array('id' => $request_id),
            array('%s', '%s'),
            array('%d')
        );
        
        if ($updated) {
            do_action('gf_request_status_changed', $request_id, $new_status);
        }
        
        return $updated;
    }
}
```

---

### کلاس Request Status (class-request-status.php)

**مسئولیت:** مدیریت وضعیت‌ها و رنگ‌بندی

```php
class GF_Request_Tracker_Status {
    
    /**
     * لیست وضعیت‌های پیش‌فرض
     */
    public static function get_statuses() {
        return array(
            'pending' => array(
                'label' => 'در انتظار بررسی',
                'color' => '#FFA500',
                'icon'  => 'clock'
            ),
            'approved' => array(
                'label' => 'تایید شده',
                'color' => '#0073AA',
                'icon'  => 'check-circle'
            ),
            'in_progress' => array(
                'label' => 'در حال انجام',
                'color' => '#1E90FF',
                'icon'  => 'spinner'
            ),
            'completed' => array(
                'label' => 'تکمیل شده',
                'color' => '#28A745',
                'icon'  => 'check-double'
            ),
            'cancelled' => array(
                'label' => 'لغو شده',
                'color' => '#DC3545',
                'icon'  => 'times-circle'
            ),
            'on_hold' => array(
                'label' => 'معلق',
                'color' => '#6C757D',
                'icon'  => 'pause'
            ),
            'rejected' => array(
                'label' => 'رد شده',
                'color' => '#C82333',
                'icon'  => 'ban'
            ),
            'archived' => array(
                'label' => 'بایگانی شده',
                'color' => '#17A2B8',
                'icon'  => 'archive'
            )
        );
    }
    
    /**
     * دریافت بَج HTML وضعیت
     */
    public static function get_status_badge($status) {
        $statuses = self::get_statuses();
        
        if (!isset($statuses[$status])) {
            return '';
        }
        
        $data = $statuses[$status];
        
        return sprintf(
            '<span class="gf-status-badge" style="background-color: %s; color: white; padding: 5px 12px; border-radius: 20px; font-size: 12px;">%s</span>',
            esc_attr($data['color']),
            esc_html($data['label'])
        );
    }
}
```

---

### کلاس Frontend (class-frontend.php)

**مسئولیت:** نمایش صفحه پیگیری به کاربران

```php
class GF_Request_Tracker_Frontend {
    
    public function __construct() {
        add_shortcode('gf_request_tracker_page', array($this, 'render_tracking_page'));
        add_action('wp_enqueue_scripts', array($this, 'enqueue_assets'));
    }
    
    /**
     * بارگذاری CSS و JS
     */
    public function enqueue_assets() {
        if (is_page() && has_shortcode(get_post()->post_content, 'gf_request_tracker_page')) {
            
            wp_enqueue_style(
                'gf-tracker-frontend',
                GF_TRACKER_URL . 'assets/css/frontend.css',
                array(),
                GF_TRACKER_VERSION
            );
            
            wp_enqueue_script(
                'gf-tracker-frontend',
                GF_TRACKER_URL . 'assets/js/frontend.js',
                array('jquery'),
                GF_TRACKER_VERSION,
                true
            );
            
            wp_localize_script('gf-tracker-frontend', 'gfTrackerData', array(
                'ajax_url' => admin_url('admin-ajax.php'),
                'nonce'    => wp_create_nonce('gf_tracker_nonce')
            ));
        }
    }
    
    /**
     * رندر صفحه پیگیری
     */
    public function render_tracking_page() {
        if (!is_user_logged_in()) {
            return '<p>لطفاً برای مشاهده درخواست‌ها وارد شوید.</p>';
        }
        
        $user_id = get_current_user_id();
        
        ob_start();
        ?>
        <div class="gf-tracker-container">
            
            <div class="gf-tracker-header">
                <h2>درخواست‌های من</h2>
                <div class="gf-tracker-tabs">
                    <button class="gf-tab active" data-tab="active">
                        فعال
                    </button>
                    <button class="gf-tab" data-tab="history">
                        تاریخچه
                    </button>
                </div>
            </div>
            
            <div id="active-requests" class="gf-tab-content active">
                <?php $this->render_active_requests($user_id); ?>
            </div>
            
            <div id="history-requests" class="gf-tab-content">
                <?php $this->render_history_requests($user_id); ?>
            </div>
            
        </div>
        <?php
        return ob_get_clean();
    }
    
    /**
     * نمایش درخواست‌های فعال
     */
    private function render_active_requests($user_id) {
        $active_statuses = array('pending', 'approved', 'in_progress', 'on_hold');
        
        foreach ($active_statuses as $status) {
            $requests = GF_Request_Tracker_Database::get_user_requests($user_id, $status);
            
            if (!empty($requests)) {
                foreach ($requests as $request) {
                    $this->render_request_card($request);
                }
            }
        }
    }
    
    /**
     * رندر کارت درخواست
     */
    private function render_request_card($request) {
        $entry = GFAPI::get_entry($request->entry_id);
        $form = GFAPI::get_form($request->form_id);
        
        ?>
        <div class="gf-request-card" data-request-id="<?php echo $request->id; ?>">
            
            <div class="gf-request-header">
                <span class="gf-request-id">#<?php echo $request->id; ?></span>
                <?php echo GF_Request_Tracker_Status::get_status_badge($request->status); ?>
            </div>
            
            <div class="gf-request-body">
                <h3><?php echo esc_html($form['title']); ?></h3>
                
                <?php foreach ($form['fields'] as $field): ?>
                    <?php if (!in_array($field->type, array('page', 'section', 'html'))): ?>
                        <div class="gf-field-value">
                            <strong><?php echo $field->label; ?>:</strong>
                            <?php echo rgar($entry, $field->id); ?>
                        </div>
                    <?php endif; ?>
                <?php endforeach; ?>
            </div>
            
            <div class="gf-request-footer">
                <span class="gf-request-date">
                    <?php echo date_i18n('Y/m/d - H:i', strtotime($request->created_at)); ?>
                </span>
            </div>
            
        </div>
        <?php
    }
}
```

---

### کلاس AJAX (class-ajax.php)

**مسئولیت:** مدیریت درخواست‌های AJAX

```php
class GF_Request_Tracker_AJAX {
    
    public function __construct() {
        add_action('wp_ajax_gf_update_status', array($this, 'update_status'));
        add_action('wp_ajax_gf_get_request_details', array($this, 'get_request_details'));
    }
    
    /**
     * به‌روزرسانی وضعیت درخواست
     */
    public function update_status() {
        check_ajax_referer('gf_tracker_nonce', 'nonce');
        
        if (!current_user_can('manage_options')) {
            wp_send_json_error('دسترسی غیرمجاز');
        }
        
        $request_id = intval($_POST['request_id']);
        $new_status = sanitize_text_field($_POST['status']);
        
        $updated = GF_Request_Tracker_Database::update_status($request_id, $new_status);
        
        if ($updated) {
            wp_send_json_success(array(
                'message' => 'وضعیت با موفقیت تغییر کرد',
                'badge'   => GF_Request_Tracker_Status::get_status_badge($new_status)
            ));
        } else {
            wp_send_json_error('خطا در به‌روزرسانی');
        }
    }
}
```

---

### کلاس Notifications (class-notifications.php)

**مسئولیت:** ارسال اعلان‌های ایمیل

```php
class GF_Request_Tracker_Notifications {
    
    public function __construct() {
        add_action('gf_request_status_changed', array($this, 'send_status_email'), 10, 2);
    }
    
    /**
     * ارسال ایمیل تغییر وضعیت
     */
    public function send_status_email($request_id, $new_status) {
        global $wpdb;
        
        $request = $wpdb->get_row($wpdb->prepare(
            "SELECT * FROM {$wpdb->prefix}gf_request_tracker WHERE id = %d",
            $request_id
        ));
        
        if (!$request || !$request->user_email) {
            return;
        }
        
        $status_data = GF_Request_Tracker_Status::get_statuses()[$new_status];
        
        $to = $request->user_email;
        $subject = 'تغییر وضعیت درخواست شما';
        
        ob_start();
        ?>
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Tahoma, Arial; direction: rtl; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: #0073aa; color: white; padding: 20px; }
                .content { background: #f9f9f9; padding: 20px; }
                .status-badge { 
                    display: inline-block;
                    background: <?php echo $status_data['color']; ?>;
                    color: white;
                    padding: 8px 16px;
                    border-radius: 20px;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h2>وضعیت درخواست شما تغییر کرد</h2>
                </div>
                <div class="content">
                    <p>درخواست شماره <strong>#<?php echo $request->id; ?></strong> به وضعیت زیر تغییر یافت:</p>
                    <p>
                        <span class="status-badge"><?php echo $status_data['label']; ?></span>
                    </p>
                    <p>
                        <a href="<?php echo home_url('/track-requests/'); ?>">مشاهده جزئیات درخواست</a>
                    </p>
                </div>
            </div>
        </body>
        </html>
        <?php
        $message = ob_get_clean();
        
        $headers = array(
            'Content-Type: text/html; charset=UTF-8',
            'From: ' . get_bloginfo('name') . ' <' . get_option('admin_email') . '>'
        );
        
        wp_mail($to, $subject, $message, $headers);
    }
}
```

---

## ۵. اتصال به Gravity Forms

### نحوه دریافت درخواست‌های جدید

افزونه به صورت خودکار از Hook زیر استفاده می‌کند:

```php
add_action('gform_after_submission', 'gf_tracker_save_entry', 10, 2);

function gf_tracker_save_entry($entry, $form) {
    // ذخیره در جدول سفارشی
    GF_Request_Tracker_Database::save_request($entry, $form);
    
    // ارسال ایمیل تایید
    do_action('gf_new_request_submitted', $entry, $form);
}
```

---

## ۶. توسعه‌های آینده

### فاز دوم: API و پنل ارائه‌دهندگان

```php
// مسیرهای REST API برای اتصال خارجی
register_rest_route('gf-tracker/v1', '/provider/requests', array(
    'methods'  => 'GET',
    'callback' => 'get_provider_requests',
    'permission_callback' => 'verify_api_key'
));

register_rest_route('gf-tracker/v1', '/provider/update-status', array(
    'methods'  => 'POST',
    'callback' => 'provider_update_status',
    'permission_callback' => 'verify_api_key'
));
```

### امکانات پیشنهادی:
- نوتیفیکیشن Real-time با WebSocket
- پنل Laravel/Vue.js برای ارائه‌دهندگان
- اپلیکیشن موبایل React Native
- سیستم چت داخلی
- نقشه و GPS برای پیگیری مکانی

---

## ۷. نکات امنیتی

### Nonce Verification
```php
wp_verify_nonce($_POST['nonce'], 'gf_tracker_action');
```

### Capability Check
```php
if (!current_user_can('manage_options')) {
    wp_die('دسترسی غیرمجاز');
}
```

### Sanitization
```php
$status = sanitize_text_field($_POST['status']);
$request_id = intval($_POST['request_id']);
```

---

## ۸. عیب‌یابی

### مشکلات رایج:

**جداول ایجاد نمی‌شوند:**
```sql
-- اجرای دستی در phpMyAdmin
CREATE TABLE wp_gf_request_tracker (...)
```

**CSS/JS لود نمی‌شود:**
```php
// پاک کردن کش
wp_cache_flush();
```

**ایمیل‌ها ارسال نمی‌شوند:**
```php
// تست SMTP
add_action('phpmailer_init', 'configure_smtp');
```

---

## ۹. مجوز و پشتیبانی

**مجوز:** GPL v2 یا بالاتر  
**نسخه:** 1.0.0  
**توسعه‌دهنده:** تیم توسعه پروژه
