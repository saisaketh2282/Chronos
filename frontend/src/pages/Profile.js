import React from 'react';
import { useQuery } from 'react-query';
import { User, Mail, Shield, Clock, Activity } from 'lucide-react';
import { authAPI } from '../services/api';

function Profile() {
  const { data: userData, isLoading } = useQuery(
    'currentUser',
    () => authAPI.getCurrentUser(),
    { retry: false }
  );

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  const user = userData?.data;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Profile</h1>
        <p className="mt-1 text-sm text-gray-500">
          Your account information and system access
        </p>
      </div>

      {/* User Information */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="card">
          <div className="card-header">
            <h3 className="text-lg font-medium text-gray-900">Account Information</h3>
          </div>
          <div className="card-body space-y-4">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="h-12 w-12 rounded-full bg-primary-100 flex items-center justify-center">
                  <User className="h-6 w-6 text-primary-600" />
                </div>
              </div>
              <div className="ml-4">
                <h4 className="text-lg font-medium text-gray-900">{user?.username}</h4>
                <p className="text-sm text-gray-500">System User</p>
              </div>
            </div>

            <div className="space-y-3">
              <div className="flex items-center">
                <Shield className="h-4 w-4 text-gray-400 mr-3" />
                <div>
                  <label className="text-sm font-medium text-gray-500">Role</label>
                  <p className="text-sm text-gray-900">
                    {user?.authorities?.map(auth => auth.authority).join(', ') || 'User'}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <h3 className="text-lg font-medium text-gray-900">System Information</h3>
          </div>
          <div className="card-body space-y-4">
            <div className="space-y-3">
              <div className="flex items-center">
                <Clock className="h-4 w-4 text-gray-400 mr-3" />
                <div>
                  <label className="text-sm font-medium text-gray-500">Current Time</label>
                  <p className="text-sm text-gray-900">
                    {new Date().toLocaleString()}
                  </p>
                </div>
              </div>
              <div className="flex items-center">
                <Activity className="h-4 w-4 text-gray-400 mr-3" />
                <div>
                  <label className="text-sm font-medium text-gray-500">Session Status</label>
                  <p className="text-sm text-green-600">Active</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* System Features */}
      <div className="card">
        <div className="card-header">
          <h3 className="text-lg font-medium text-gray-900">Available Features</h3>
        </div>
        <div className="card-body">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <div className="flex items-start">
              <div className="flex-shrink-0">
                <div className="h-8 w-8 rounded-lg bg-blue-100 flex items-center justify-center">
                  <Activity className="h-4 w-4 text-blue-600" />
                </div>
              </div>
              <div className="ml-3">
                <h4 className="text-sm font-medium text-gray-900">Job Management</h4>
                <p className="text-sm text-gray-500">Create, schedule, and monitor jobs</p>
              </div>
            </div>

            <div className="flex items-start">
              <div className="flex-shrink-0">
                <div className="h-8 w-8 rounded-lg bg-green-100 flex items-center justify-center">
                  <Clock className="h-4 w-4 text-green-600" />
                </div>
              </div>
              <div className="ml-3">
                <h4 className="text-sm font-medium text-gray-900">Scheduling</h4>
                <p className="text-sm text-gray-500">One-time and recurring job scheduling</p>
              </div>
            </div>

            <div className="flex items-start">
              <div className="flex-shrink-0">
                <div className="h-8 w-8 rounded-lg bg-yellow-100 flex items-center justify-center">
                  <Shield className="h-4 w-4 text-yellow-600" />
                </div>
              </div>
              <div className="ml-3">
                <h4 className="text-sm font-medium text-gray-900">Security</h4>
                <p className="text-sm text-gray-500">JWT-based authentication and authorization</p>
              </div>
            </div>

            <div className="flex items-start">
              <div className="flex-shrink-0">
                <div className="h-8 w-8 rounded-lg bg-purple-100 flex items-center justify-center">
                  <Mail className="h-4 w-4 text-purple-600" />
                </div>
              </div>
              <div className="ml-3">
                <h4 className="text-sm font-medium text-gray-900">Notifications</h4>
                <p className="text-sm text-gray-500">Email alerts for job completion and failures</p>
              </div>
            </div>

            <div className="flex items-start">
              <div className="flex-shrink-0">
                <div className="h-8 w-8 rounded-lg bg-red-100 flex items-center justify-center">
                  <Activity className="h-4 w-4 text-red-600" />
                </div>
              </div>
              <div className="ml-3">
                <h4 className="text-sm font-medium text-gray-900">Monitoring</h4>
                <p className="text-sm text-gray-500">Real-time job monitoring and logging</p>
              </div>
            </div>

            <div className="flex items-start">
              <div className="flex-shrink-0">
                <div className="h-8 w-8 rounded-lg bg-indigo-100 flex items-center justify-center">
                  <Shield className="h-4 w-4 text-indigo-600" />
                </div>
              </div>
              <div className="ml-3">
                <h4 className="text-sm font-medium text-gray-900">Retry Logic</h4>
                <p className="text-sm text-gray-500">Automatic retry with exponential backoff</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="card">
        <div className="card-header">
          <h3 className="text-lg font-medium text-gray-900">Quick Actions</h3>
        </div>
        <div className="card-body">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <a
              href="/jobs/create"
              className="flex items-center justify-center px-4 py-3 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              <Activity className="h-4 w-4 mr-2" />
              Create Job
            </a>
            <a
              href="/jobs"
              className="flex items-center justify-center px-4 py-3 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              <Clock className="h-4 w-4 mr-2" />
              View Jobs
            </a>
            <a
              href="/admin"
              className="flex items-center justify-center px-4 py-3 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              <Shield className="h-4 w-4 mr-2" />
              Admin Panel
            </a>
            <a
              href="/"
              className="flex items-center justify-center px-4 py-3 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              <Activity className="h-4 w-4 mr-2" />
              Dashboard
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Profile;
