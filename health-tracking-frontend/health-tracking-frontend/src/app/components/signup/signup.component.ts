import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css'],
  standalone: false,
})
export class SignupComponent implements OnInit {
  signupForm!: FormGroup;
  isDoctor: boolean = false;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.signupForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      username: ['', Validators.required], // Hem doktorlar hem de kullanıcılar için
      password: ['', Validators.required],
      role: ['USER', Validators.required],
      specialization: [''], // Sadece doktorlar için gerekli
    });

    // Dinamik alanları kontrol etmek için role değişikliğini dinleyin
    this.signupForm.get('role')?.valueChanges.subscribe((role) => {
      this.isDoctor = role === 'DOCTOR';
      if (this.isDoctor) {
        this.signupForm.get('specialization')?.setValidators(Validators.required);
      } else {
        this.signupForm.get('specialization')?.clearValidators();
      }
      this.signupForm.get('specialization')?.updateValueAndValidity();
    });
  }

  onSubmit(): void {
    if (this.signupForm.valid) {
      this.http
        .post('http://localhost:8080/api/users/signup', this.signupForm.value)
        .subscribe({
          next: (response: any) => {
            alert(response.message);
            this.router.navigate(['/login']);
          },
          error: (error) => {
            console.error(error);
            alert(error.error.message || 'Signup failed. Please try again.');
          },
        });
    }
  }
}