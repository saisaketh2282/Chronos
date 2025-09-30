# Chronos Frontend

A modern, responsive React frontend for the Chronos Job Scheduler system.

## 🚀 Features

- **Modern UI/UX**: Clean, intuitive interface built with React and Tailwind CSS
- **Real-time Updates**: Live job status updates and monitoring
- **Responsive Design**: Works seamlessly on desktop, tablet, and mobile devices
- **Authentication**: Secure JWT-based login system
- **Job Management**: Create, edit, cancel, and monitor jobs
- **Admin Panel**: System administration and maintenance tools
- **Charts & Analytics**: Visual job statistics and performance metrics
- **Dark/Light Mode**: Toggle between themes (coming soon)

## 🛠️ Tech Stack

- **React 18**: Modern React with hooks and functional components
- **React Router**: Client-side routing
- **React Query**: Data fetching and caching
- **Tailwind CSS**: Utility-first CSS framework
- **Lucide React**: Beautiful, customizable icons
- **Recharts**: Composable charting library
- **React Hook Form**: Form handling and validation
- **Axios**: HTTP client for API communication
- **React Hot Toast**: Beautiful toast notifications

## 📦 Installation

1. **Prerequisites**
   - Node.js 16+ and npm
   - Chronos backend running on port 8080

2. **Install Dependencies**
   ```bash
   cd frontend
   npm install
   ```

3. **Environment Configuration**
   Create a `.env` file in the frontend directory:
   ```env
   REACT_APP_API_URL=http://localhost:8080/api
   ```

4. **Start Development Server**
   ```bash
   npm start
   ```

   The application will open at `http://localhost:3000`

## 🏗️ Project Structure

```
frontend/
├── public/
│   ├── index.html
│   └── manifest.json
├── src/
│   ├── components/
│   │   └── Layout.js          # Main layout component
│   ├── contexts/
│   │   └── AuthContext.js     # Authentication context
│   ├── pages/
│   │   ├── Login.js           # Login page
│   │   ├── Dashboard.js       # Main dashboard
│   │   ├── Jobs.js            # Job management
│   │   ├── JobDetails.js      # Job details and logs
│   │   ├── CreateJob.js       # Job creation form
│   │   ├── Admin.js           # Admin panel
│   │   └── Profile.js         # User profile
│   ├── services/
│   │   └── api.js             # API service layer
│   ├── App.js                 # Main app component
│   ├── index.js               # App entry point
│   └── index.css              # Global styles
├── package.json
├── tailwind.config.js
└── README.md
```

## 🎨 UI Components

### Layout Components
- **Layout**: Main application layout with sidebar navigation
- **Navigation**: Responsive sidebar with role-based menu items
- **Header**: Top bar with user info and system status

### Page Components
- **Dashboard**: Overview with statistics, charts, and recent jobs
- **Jobs**: Job listing with filtering, sorting, and pagination
- **JobDetails**: Detailed job view with logs and execution history
- **CreateJob**: Form for creating one-time, recurring, and batch jobs
- **Admin**: System administration and maintenance tools
- **Profile**: User profile and system information

### Reusable Components
- **StatCard**: Statistics display cards
- **StatusBadge**: Job status indicators
- **LoadingSpinner**: Loading state indicator
- **FormInput**: Styled form inputs
- **Button**: Consistent button styling

## 🔐 Authentication

The frontend uses JWT-based authentication:

1. **Login**: Users authenticate with username/password
2. **Token Storage**: JWT tokens are stored in localStorage
3. **Auto-refresh**: Tokens are automatically validated on app load
4. **Role-based Access**: Different features based on user roles

### Demo Credentials
- **Admin**: `admin` / `admin123`
- **User**: `user` / `user123`
- **Scheduler**: `scheduler` / `scheduler123`

## 📊 Features Overview

### Dashboard
- Real-time job statistics
- Visual charts and graphs
- Recent job activity
- System health status

### Job Management
- Create one-time, recurring, and batch jobs
- Filter and search jobs
- Real-time status updates
- Job cancellation and deletion
- Detailed execution logs

### Admin Panel
- System statistics and health
- Stuck job detection and recovery
- Old job cleanup
- System maintenance tools

### Monitoring
- Real-time job status updates
- Execution logs with filtering
- Performance metrics
- Error tracking and reporting

## 🎨 Styling

The application uses Tailwind CSS with a custom design system:

### Color Palette
- **Primary**: Blue tones for main actions
- **Success**: Green for completed states
- **Warning**: Yellow for pending states
- **Danger**: Red for errors and failures
- **Neutral**: Gray tones for text and backgrounds

### Typography
- **Font**: Inter (system font stack)
- **Headings**: Bold, clear hierarchy
- **Body**: Readable, accessible text

### Components
- **Cards**: Subtle shadows and rounded corners
- **Buttons**: Consistent sizing and hover states
- **Forms**: Clean inputs with validation states
- **Tables**: Responsive with hover effects

## 🔧 Configuration

### API Configuration
The frontend connects to the backend API through the `api.js` service:

```javascript
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
```

### Query Configuration
React Query is configured for optimal performance:
- 5-minute stale time for most queries
- Automatic refetch on window focus disabled
- Retry logic for failed requests
- Background refetching for real-time updates

## 🚀 Deployment

### Build for Production
```bash
npm run build
```

### Environment Variables
Set the following environment variables for production:
```env
REACT_APP_API_URL=https://your-api-domain.com/api
```

### Static Hosting
The built application can be deployed to any static hosting service:
- Vercel
- Netlify
- AWS S3 + CloudFront
- GitHub Pages

## 🧪 Testing

### Run Tests
```bash
npm test
```

### Test Coverage
```bash
npm run test:coverage
```

## 📱 Responsive Design

The application is fully responsive and works on:
- **Desktop**: Full-featured experience
- **Tablet**: Optimized layout with collapsible sidebar
- **Mobile**: Mobile-first design with touch-friendly controls

## 🔄 Real-time Updates

The frontend provides real-time updates through:
- **Polling**: Regular API calls for job status updates
- **React Query**: Automatic background refetching
- **Visual Indicators**: Live status badges and progress indicators

## 🎯 Performance

### Optimization Features
- **Code Splitting**: Lazy loading of route components
- **Memoization**: React.memo for expensive components
- **Query Caching**: Intelligent data caching with React Query
- **Bundle Optimization**: Tree shaking and minification

### Performance Metrics
- **First Contentful Paint**: < 1.5s
- **Largest Contentful Paint**: < 2.5s
- **Time to Interactive**: < 3s
- **Bundle Size**: < 500KB gzipped

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Chronos Frontend** - Modern, responsive, and user-friendly job scheduler dashboard.
