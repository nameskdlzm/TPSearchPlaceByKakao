package com.mrhi2024.tpsearchplacebykakao.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.mrhi2024.tpsearchplacebykakao.R
import com.mrhi2024.tpsearchplacebykakao.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //둘러보기 글씨 클릭으로 로그인없이 Main화면으로 이동
        binding.tvGo.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        //회원가입 버튼 클릭
        binding.tvSignup.setOnClickListener { startActivity(Intent(this, SignupActivity::class.java)) }

        //이메일 로그인 버튼 클릭
        binding.layoutEmailLogin.setOnClickListener { startActivity(Intent(this, EmailLoginActivity::class.java)) }


        //간편로그인 버튼들 클릭
        binding.btnLoginKakao.setOnClickListener { clickKakao() }
        binding.btnLoginGoogle.setOnClickListener { clickGoogle() }
        binding.btnLoginNaver.setOnClickListener { clickNaver() }
    }

    private fun clickKakao(){
        Toast.makeText(this, "카카오로그인", Toast.LENGTH_SHORT).show()

    }

    private fun clickGoogle(){
        Toast.makeText(this, "구글로그인", Toast.LENGTH_SHORT).show()
    }

    private fun clickNaver(){
        Toast.makeText(this, "네이버로그인", Toast.LENGTH_SHORT).show()
    }
}