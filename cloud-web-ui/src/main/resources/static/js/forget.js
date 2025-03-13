// 忘记密码，异步请求，点击不刷新页面
$(function (){
    // 这个得是button按钮才行
    $("#verifyCodeBtn").click(sendCode);
});

function sendCode(){
    const $btn = $('#verifyCodeBtn');
    const $emailInput = $('#email');

    // 立即禁用按钮防止重复点击
    $btn.prop('disabled', true);

    // 清空旧提示
    $emailInput.removeClass('is-invalid');
    $('#emailMsg').html('');

    const email = $emailInput.val().trim();

    // 邮箱验证
    if (!validateEmail(email)) {
        $emailInput.addClass('is-invalid');
        $('#emailMsg').html("请输入有效的邮箱地址");
        $btn.prop('disabled', false); // 验证失败恢复按钮
        return false;
    }

    // 异步请求
    $.post("/send/verifyCode", {"email": email})
        .done(function(response) {
            if (response.code == '200') {
                startCountdown($btn);
            } else {
                alert('发送失败，请稍后重试');
                $btn.prop('disabled', false);
            }
        })
        .fail(function(xhr){
            alert('发送失败，请稍后重试');
            $btn.prop('disabled', false);
        });

    return false;
}

// 显示消息的通用方法
function showMessage(text, type) {
    $('#verifyCodeMsg')
        .html(text)
        .addClass(type);
}

// 倒计时逻辑
function startCountdown($btn) {
    let countdown = 60;
    const timer = setInterval(() => {
        $btn.text(`重新发送(${countdown})`);
        if (--countdown < 0) {
            clearInterval(timer);
            $btn.text('获取验证码').prop('disabled', false);
        }
    }, 1000);
}

// 简单的邮箱格式验证
function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}