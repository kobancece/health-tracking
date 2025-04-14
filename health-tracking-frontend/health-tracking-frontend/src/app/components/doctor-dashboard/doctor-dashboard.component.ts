import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-doctor-dashboard',
  standalone: false,
  
  templateUrl: './doctor-dashboard.component.html',
  styleUrl: './doctor-dashboard.component.css'
})

export class DoctorDashboardComponent implements OnInit {
  patients: any[] = []; // Doktorun hastalarını tutar
  messages: any[] = []; // Doktorun gelen mesajlarını tutar
  selectedMessage: any = null; // Seçilen mesajın detayları
  replyText: string = ''; // Cevap metni
  doctorId: string | null = null; // Doktorun user tablosundaki document ID'si
  userId: string | null = null; // Kullanıcının user tablosundaki document ID'si

  messageText: string = ''; // Mesaj kutusundaki metin
  selectedUser: any = null; // Mesaj göndermek için seçilen kullanıcı bilgisi
  selectedUserId: string | null = null; // Seçilen kullanıcının user tablosundaki document ID'si
  selectedUserUsername: string | null = null; // Seçilen kullanıcının kullanıcı adı

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.doctorId = localStorage.getItem('userId'); // Doktorun ID'si (user tablosundan)
    if (this.doctorId) {
      this.loadMessages();
      this.loadPatients();
    } else {
      console.error('Doctor ID is missing in localStorage!');
    }
  }

  loadMessages(): void {
    if (!this.doctorId) {
      console.error('Doctor ID not found in localStorage.');
      return;
    }

    this.http.get<any>(`http://localhost:8080/api/messages/inbox/${this.doctorId}`).subscribe({
      next: (response) => {
        console.log('Messages loaded for doctor:', response);
        this.messages = response.messages; // Gelen mesajları yükle
      },
      error: (error) => {
        console.error('Error loading messages:', error);
      }
    });
  }

  loadPatients(): void {
    if (!this.doctorId) {
      console.error('Doctor ID not found in localStorage.');
      return;
    }

    this.http.get<any[]>(`http://localhost:8080/api/messages/patients/${this.doctorId}`).subscribe({
      next: (response) => {
        console.log('Patients loaded:', response);
        this.patients = response;
      },
      error: (error) => {
        console.error('Error loading patients:', error);
      }
    });
  }

  downloadPatientInfo(patientId: string): void {
    this.http.get(`http://localhost:8080/api/users/${patientId}/download`, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = `patient-${patientId}.xml`;
        anchor.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error downloading patient info:', error);
      }
    });
  }

  selectMessage(message: any): void {
    this.selectedMessage = message;
    this.replyText = ''; // Reply alanını temizle
    if (!message.isRead) {
      this.markMessageAsRead(message.id);
    }
  }

  closeMessage(): void {
    this.selectedMessage = null;
  }

  markMessageAsRead(messageId: string): void {
    this.http.put(`http://localhost:8080/api/messages/${messageId}/read`, {}).subscribe({
      next: () => {
        console.log('Message marked as read:', messageId);
        const message = this.messages.find((msg) => msg.id === messageId);
        if (message) {
          message.isRead = true;
        }
      },
      error: (error) => {
        console.error('Error marking message as read:', error);
      }
    });
  }

  sendReply(): void {
    if (!this.selectedMessage || !this.replyText.trim()) {
      alert('Please write a reply before sending.');
      return;
    }

    const replyData = {
      senderId: this.doctorId,
      receiverId: this.selectedMessage.senderId, // Mesajı gönderen kullanıcıya cevap
      messageText: this.replyText.trim(),
      senderRole: 'DOCTOR',
      receiverRole: 'USER',
      isRead: false
    };

    this.http.post('http://localhost:8080/api/messages/send', replyData).subscribe({
      next: () => {
        alert('Reply sent successfully!');
        this.replyText = ''; // Reply alanını temizle
      },
      error: (error) => {
        console.error('Error sending reply:', error);
      }
    });
  }

  toggleMessageBox(user: any): void {
    if (!user || !user.id) {
      console.warn('Invalid user selected.');
      return;
    }

    this.selectedUser = user;
    this.selectedUserId = user.id; // Kullanıcının userId'si
    this.selectedUserUsername = user.username; // Kullanıcının username'i
    this.messageText = ''; // Mesaj kutusunu temizle
  }

  sendMessage(): void {
    if (!this.selectedUserId || !this.messageText.trim()) {
      alert('Please select a user and write a message.');
      return;
    }

    const messageData = {
      senderId: this.doctorId, // Doktorun user tablosundaki document ID'si
      receiverId: this.selectedUserId, // Seçilen kullanıcının user tablosundaki document ID'si
      messageText: this.messageText.trim(),
      senderRole: 'DOCTOR', // Gönderici rolü doktor
      receiverRole: 'USER', // Alıcı rolü kullanıcı
      isRead: false // Mesaj okunmamış
    };

    this.http.post('http://localhost:8080/api/messages/send', messageData).subscribe({
      next: () => {
        alert('Message sent successfully!');
        this.messageText = ''; // Mesaj kutusunu temizle
      },
      error: (error) => {
        console.error('Error sending message:', error);
      }
    });
  }

  cancelMessage(): void {
    this.selectedUser = null;
    this.selectedUserId = null;
    this.selectedUserUsername = null;
  }
}