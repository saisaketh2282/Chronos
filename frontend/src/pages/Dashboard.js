import React from 'react';
import { useQuery } from 'react-query';
import { 
  Calendar, 
  Clock, 
  CheckCircle, 
  XCircle, 
  AlertCircle,
  TrendingUp,
  Activity
} from 'lucide-react';
import { jobsAPI, healthAPI } from '../services/api';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';

const COLORS = {
  scheduled: '#3b82f6',
  running: '#f59e0b',
  completed: '#22c55e',
  failed: '#ef4444',
  cancelled: '#6b7280',
  retrying: '#f97316'
};

function StatCard({ title, value, icon: Icon, color, trend }) {
  return (
    <div className="card">
      <div className="card-body">
        <div className="flex items-center">
          <div className={`p-3 rounded-lg ${color}`}>
            <Icon className="h-6 w-6 text-white" />
          </div>
          <div className="ml-4">
            <p className="text-sm font-medium text-gray-500">{title}</p>
            <p className="text-2xl font-semibold text-gray-900">{value}</p>
            {trend && (
              <p className={`text-sm ${trend > 0 ? 'text-green-600' : 'text-red-600'}`}>
                {trend > 0 ? '+' : ''}{trend}% from last week
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function Dashboard() {
  const { data: stats, isLoading: statsLoading } = useQuery(
    'jobStatistics',
    () => jobsAPI.getJobStatistics(),
    { refetchInterval: 30000 }
  );

  const { data: health, isLoading: healthLoading } = useQuery(
    'health',
    () => healthAPI.getHealth(),
    { refetchInterval: 10000 }
  );

  const { data: recentJobs } = useQuery(
    'recentJobs',
    () => jobsAPI.getJobs({ page: 0, size: 5, sortBy: 'createdAt', sortDir: 'desc' })
  );

  if (statsLoading || healthLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  const jobStats = stats?.data || {};
  const healthData = health?.data || {};

  // Prepare chart data
  const statusData = [
    { name: 'Scheduled', value: jobStats.scheduledJobs || 0, color: COLORS.scheduled },
    { name: 'Running', value: jobStats.runningJobs || 0, color: COLORS.running },
    { name: 'Completed', value: jobStats.completedJobs || 0, color: COLORS.completed },
    { name: 'Failed', value: jobStats.failedJobs || 0, color: COLORS.failed },
  ];

  const typeData = [
    { name: 'One-time', value: jobStats.oneTimeJobs || 0 },
    { name: 'Recurring', value: jobStats.recurringJobs || 0 },
    { name: 'Batch', value: jobStats.batchJobs || 0 },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="mt-1 text-sm text-gray-500">
          Overview of your job scheduling system
        </p>
      </div>

      {/* System Status */}
      <div className="card">
        <div className="card-header">
          <h3 className="text-lg font-medium text-gray-900">System Status</h3>
        </div>
        <div className="card-body">
          <div className="flex items-center space-x-6">
            <div className="flex items-center">
              <div className={`h-3 w-3 rounded-full ${healthData.status === 'UP' ? 'bg-green-400' : 'bg-red-400'}`}></div>
              <span className="ml-2 text-sm text-gray-600">
                {healthData.status === 'UP' ? 'System Online' : 'System Offline'}
              </span>
            </div>
            <div className="text-sm text-gray-500">
              Last updated: {new Date().toLocaleTimeString()}
            </div>
          </div>
        </div>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="Total Jobs"
          value={jobStats.totalJobs || 0}
          icon={Calendar}
          color="bg-blue-500"
        />
        <StatCard
          title="Running Jobs"
          value={jobStats.runningJobs || 0}
          icon={Activity}
          color="bg-yellow-500"
        />
        <StatCard
          title="Completed"
          value={jobStats.completedJobs || 0}
          icon={CheckCircle}
          color="bg-green-500"
        />
        <StatCard
          title="Failed"
          value={jobStats.failedJobs || 0}
          icon={XCircle}
          color="bg-red-500"
        />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Job Status Distribution */}
        <div className="card">
          <div className="card-header">
            <h3 className="text-lg font-medium text-gray-900">Job Status Distribution</h3>
          </div>
          <div className="card-body">
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={statusData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={100}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {statusData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="mt-4 grid grid-cols-2 gap-4">
              {statusData.map((item) => (
                <div key={item.name} className="flex items-center">
                  <div 
                    className="h-3 w-3 rounded-full mr-2" 
                    style={{ backgroundColor: item.color }}
                  ></div>
                  <span className="text-sm text-gray-600">{item.name}: {item.value}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Job Types */}
        <div className="card">
          <div className="card-header">
            <h3 className="text-lg font-medium text-gray-900">Job Types</h3>
          </div>
          <div className="card-body">
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={typeData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="value" fill="#3b82f6" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Jobs */}
      <div className="card">
        <div className="card-header">
          <h3 className="text-lg font-medium text-gray-900">Recent Jobs</h3>
        </div>
        <div className="card-body">
          {recentJobs?.data?.content?.length > 0 ? (
            <div className="overflow-hidden">
              <table className="table">
                <thead>
                  <tr>
                    <th>Job Name</th>
                    <th>Type</th>
                    <th>Status</th>
                    <th>Created</th>
                    <th>Created By</th>
                  </tr>
                </thead>
                <tbody>
                  {recentJobs.data.content.map((job) => (
                    <tr key={job.id}>
                      <td className="font-medium">{job.name}</td>
                      <td>
                        <span className="status-badge bg-blue-100 text-blue-800">
                          {job.jobType}
                        </span>
                      </td>
                      <td>
                        <span className={`status-badge status-${job.status.toLowerCase()}`}>
                          {job.status}
                        </span>
                      </td>
                      <td>{new Date(job.createdAt).toLocaleString()}</td>
                      <td>{job.createdBy}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-8">
              <Calendar className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">No jobs</h3>
              <p className="mt-1 text-sm text-gray-500">Get started by creating a new job.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
