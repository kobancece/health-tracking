import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: false,
  
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})

export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  loginError: string = '';

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
    });
  }

  onSubmit() {
    if (this.loginForm.valid) {
      const loginData = this.loginForm.value;
      this.http.post('http://localhost:8080/api/users/login', loginData, { responseType: 'json' }).subscribe(
        (response: any) => {
          console.log('Login Response:', response);
  
          // Giriş yapan kullanıcının username'ini localStorage'da sakla
          localStorage.setItem('username', loginData.username);
          localStorage.setItem('userId', response.userId);
          console.log('Login Responseeee userrrıddd:', response.userId);
          console.log('Login Responseeee usernamm:', loginData.username);
          if (response.role === 'DOCTOR' && response.doctorId) {
            localStorage.setItem('doctorId', response.doctorId);
            console.log('Login Responseeee doctorId:', response.doctorId);
          }

          // Kullanıcı tipine göre yönlendirme
          if (response.role === 'USER') {
            this.router.navigate(['/user-dashboard']);
          } else if (response.role === 'DOCTOR') {
            this.router.navigate(['/doctor-dashboard']);
          }
        },
        (error) => {
          console.error('Login failed:', error);
          alert('Login failed: ' + error.message);
        }
      );
    }
  }

}
