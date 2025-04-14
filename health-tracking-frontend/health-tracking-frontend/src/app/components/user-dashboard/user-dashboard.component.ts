import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';


@Component({
  selector: 'app-user-dashboard',
  standalone: false,
  
  templateUrl: './user-dashboard.component.html',
  styleUrl: './user-dashboard.component.css'
})

export class UserDashboardComponent implements OnInit {
  healthForm: FormGroup;
  username: string | null = null;
  isEditing: boolean = false;
  hasHealthData: boolean = false;
  userId: string | null = null;

  messageText: string = '';
  selectedDoctor: any = null;
  selectedDoctorUserId: string | null = null;
  messages: any[] = [];
  specializations: string[] = [];
  filteredDoctors: any[] = [];
  selectedSpecialization: string = '';
  sentMessages: any[] = [];
  showSentMessages: boolean = false;
  messageSent: boolean = false;
  selectedMessage: any = null; // Seçilen mesajın detayları

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {
    this.healthForm = this.fb.group({
      weight: [null, [Validators.required, Validators.min(0)]],
      height: [null, [Validators.required, Validators.min(0)]],
      bloodPressure: ['', Validators.required],
      heartRate: [null, [Validators.required, Validators.min(0)]],
    });
  }

  ngOnInit(): void {
    this.username = localStorage.getItem('username');
    this.userId = localStorage.getItem('userId');

    if (this.userId) {
      this.loadHealthRecords();
      this.loadMessages();
      this.loadDoctors();
    } else {
      console.error('User ID not found in localStorage!');
      alert('Please log in again.');
    }
  }

  loadHealthRecords(): void {
    if (!this.userId) {
      console.error('User ID is missing');
      return;
    }

    this.http.get(`http://localhost:8080/api/health-records/user/${this.userId}`).subscribe({
      next: (response: any) => {
        if (response && response.length > 0) {
          this.hasHealthData = true;
          this.healthForm.patchValue(response[0]);
        } else {
          this.hasHealthData = false;
        }
      },
      error: (error) => {
        console.error('Error loading health records:', error);
      },
    });
  }

  saveHealthRecord(): void {
    if (this.healthForm.invalid) {
      console.error('Form is invalid');
      return;
    }

    if (!this.username) {
      console.error('Username is missing');
      alert('Please log in again.');
      return;
    }

    const headers = { Username: this.username };
    const healthData = this.healthForm.value;

    this.http.post('http://localhost:8080/api/health-records/save', healthData, { headers }).subscribe({
      next: () => {
        this.isEditing = false;
        this.hasHealthData = true;
        alert('Health record updated successfully!');
      },
      error: (error) => {
        console.error('Error saving health record:', error);
        alert('Failed to save health record.');
      },
    });
  }

  editHealthData(): void {
    this.isEditing = true;
  }

  loadDoctors(): void {
    this.http.get<any[]>('http://localhost:8080/api/doctors/all').subscribe({
      next: (response) => {
        this.specializations = [
          ...new Set(response.map((doctor) => doctor.specialization)),
        ];
      },
      error: (error) => {
        console.error('Error loading doctors:', error);
      },
    });
  }

  filterDoctors(): void {
    if (!this.selectedSpecialization) {
      this.filteredDoctors = [];
      return;
    }

    this.http.get<any[]>('http://localhost:8080/api/doctors/all').subscribe({
      next: (response) => {
        this.filteredDoctors = response.filter(
          (doctor) => doctor.specialization === this.selectedSpecialization
        );
      },
      error: (error) => {
        console.error('Error filtering doctors:', error);
      },
    });
  }

  toggleMessageBox(doctor: any): void {
    if (!doctor || !doctor.userId) {
      console.warn('Invalid doctor selected.');
      return;
    }

    this.selectedDoctor = doctor;
    this.selectedDoctorUserId = doctor.userId;
    this.messageText = '';
  }

  sendMessage(): void {
    if (!this.selectedDoctorUserId || !this.messageText.trim()) {
      alert('Please select a doctor and write a message.');
      return;
    }

    const messageData = {
      senderId: this.userId,
      receiverId: this.selectedDoctorUserId,
      messageText: this.messageText.trim(),
      senderRole: 'USER',
      receiverRole: 'DOCTOR',
      isRead: false,
    };

    this.http.post('http://localhost:8080/api/messages/send', messageData).subscribe({
      next: () => {
        this.messageSent = true;
        this.messageText = '';
        setTimeout(() => {
          this.messageSent = false;
          this.selectedDoctor = null;
          this.selectedDoctorUserId = null;
        }, 2000);
        alert('Message sent successfully!');
      },
      error: (error) => {
        console.error('Error sending message:', error);
      },
    });
  }

  loadMessages(): void {
    if (!this.userId) {
      console.error('User ID is missing');
      return;
    }

    this.http.get<any>(`http://localhost:8080/api/messages/inbox/${this.userId}`).subscribe({
      next: (response) => {
        this.messages = response.messages;
      },
      error: (error) => {
        console.error('Error loading messages:', error);
      },
    });
  }

  viewSentMessages(): void {
    this.http.get<any[]>(`http://localhost:8080/api/messages/sent/${this.userId}`).subscribe({
      next: (response) => {
        this.sentMessages = response.map((msg) => ({
          text: msg.messageText,
          to: msg.receiverUsername,
        }));
        this.showSentMessages = true;
      },
      error: (error) => {
        console.error('Error loading sent messages:', error);
      },
    });
  }

  cancelMessage(): void {
    this.selectedDoctor = null;
    this.selectedDoctorUserId = null;
  }

  closeSentMessages(): void {
    this.showSentMessages = false;
  }

  selectMessage(message: any): void {
    this.selectedMessage = message; // Mesaj detayını seç
  }

  closeMessageDetails(): void {
    this.selectedMessage = null; // Seçimi temizle
  }

  navigateToHome(): void {
    // Eğer router kullanıyorsan:
    this.router.navigate(['']);
    
    // Alternatif olarak, manuel yönlendirme:
    //window.location.href = '/home';
  }
  
}