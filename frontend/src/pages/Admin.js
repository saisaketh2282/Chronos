import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { 
  AlertTriangle, 
  Trash2, 
  RefreshCw, 
  Activity, 
  Server, 
  Database,
  Clock,
  CheckCircle,
  XCircle,
  BarChart3
} from 'lucide-react';
import { adminAPI } from '../services/api';
import toast from 'react-hot-toast';

function Admin() {
  const queryClient = useQueryClient();
  const [timeoutMinutes, setTimeoutMinutes] = useState(30);
  const [cleanupDays, setCleanupDays] = useState(30);

  const { data: systemStats, isLoading: statsLoading } = useQuery(
    'systemStatistics',
    () => adminAPI.getSystemStatistics(),
    { refetchInterval: 30000 }
  );

  const { data: stuckJobs, isLoading: stuckLoading } = useQuery(
    ['stuckJobs', timeoutMinutes],
    () => adminAPI.getStuckJobs(timeoutMinutes),
    { refetchInterval: 60000 }
  );

  const { data: cleanupJobs, isLoading: cleanupLoading } = useQuery(
    ['cleanupJobs', cleanupDays],
    () => adminAPI.getJobsForCleanup(cleanupDays),
    { refetchInterval: 300000 }
  );

  const resetStuckJobsMutation = useMutation(
    () => adminAPI.resetStuckJobs(timeoutMinutes),
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries('stuckJobs');
        queryClient.invalidateQueries('systemStatistics');
        toast.success(`Reset ${response.data.resetCount} stuck jobs`);
      },
      onError: (error) => {
        toast.error(error.response?.data?.message || 'Failed to reset stuck jobs');
      },
    }
  );

  const cleanupOldJobsMutation = useMutation(
    () => adminAPI.cleanupOldJobs(cleanupDays),
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries('cleanupJobs');
        queryClient.invalidateQueries('systemStatistics');
        toast.success(`Cleaned up ${response.data.deletedCount} old jobs`);
      },
      onError: (error) => {
        toast.error(error.response?.data?.message || 'Failed to cleanup old jobs');
      },
    }
  );

  const stats = systemStats?.data || {};

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Admin Panel</h1>
        <p className="mt-1 text-sm text-gray-500">
          System administration and maintenance tools
        </p>
      </div>

      {/* System Statistics */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        <div className="card">
          <div className="card-body">
            <div className="flex items-center">
              <div className="p-3 rounded-lg bg-blue-500">
                <BarChart3 className="h-6 w-6 text-white" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Total Jobs</p>
                <p className="text-2xl font-semibold text-gray-900">{stats.totalJobs || 0}</p>
              </div>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-body">
            <div className="flex items-center">
              <div className="p-3 rounded-lg bg-yellow-500">
                <Activity className="h-6 w-6 text-white" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Running Jobs</p>
                <p className="text-2xl font-semibold text-gray-900">{stats.runningJobs || 0}</p>
              </div>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-body">
            <div className="flex items-center">
              <div className="p-3 rounded-lg bg-green-500">
                <CheckCircle className="h-6 w-6 text-white" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Completed Jobs</p>
                <p className="text-2xl font-semibold text-gray-900">{stats.completedJobs || 0}</p>
              </div>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-body">
            <div className="flex items-center">
              <div className="p-3 rounded-lg bg-red-500">
                <XCircle className="h-6 w-6 text-white" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Failed Jobs</p>
                <p className="text-2xl font-semibold text-gray-900">{stats.failedJobs || 0}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Stuck Jobs */}
      <div className="card">
        <div className="card-header">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-medium text-gray-900">Stuck Jobs</h3>
            <div className="flex items-center space-x-3">
              <div className="flex items-center space-x-2">
                <label className="text-sm text-gray-500">Timeout (minutes):</label>
                <input
                  type="number"
                  min="1"
                  max="120"
                  value={timeoutMinutes}
                  onChange={(e) => setTimeoutMinutes(parseInt(e.target.value))}
                  className="w-20 px-2 py-1 text-sm border border-gray-300 rounded"
                />
              </div>
              <button
                onClick={() => resetStuckJobsMutation.mutate()}
                disabled={resetStuckJobsMutation.isLoading || (stuckJobs?.data?.length || 0) === 0}
                className="btn btn-warning btn-sm flex items-center"
              >
                <RefreshCw className="h-4 w-4 mr-1" />
                Reset Stuck Jobs
              </button>
            </div>
          </div>
        </div>
        <div className="card-body">
          {stuckLoading ? (
            <div className="flex items-center justify-center h-32">
              <div className="loading-spinner"></div>
            </div>
          ) : stuckJobs?.data?.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="table">
                <thead>
                  <tr>
                    <th>Job Name</th>
                    <th>Status</th>
                    <th>Started At</th>
                    <th>Duration</th>
                    <th>Created By</th>
                  </tr>
                </thead>
                <tbody>
                  {stuckJobs.data.map((job) => (
                    <tr key={job.id}>
                      <td className="font-medium">{job.name}</td>
                      <td>
                        <span className="status-badge status-running">
                          {job.status}
                        </span>
                      </td>
                      <td className="text-sm text-gray-500">
                        {job.startedAt ? new Date(job.startedAt).toLocaleString() : '-'}
                      </td>
                      <td className="text-sm text-gray-500">
                        {job.startedAt 
                          ? `${Math.floor((new Date() - new Date(job.startedAt)) / 60000)} minutes`
                          : '-'
                        }
                      </td>
                      <td className="text-sm text-gray-500">{job.createdBy}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-8">
              <CheckCircle className="mx-auto h-12 w-12 text-green-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">No stuck jobs</h3>
              <p className="mt-1 text-sm text-gray-500">
                All jobs are running normally.
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Cleanup Jobs */}
      <div className="card">
        <div className="card-header">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-medium text-gray-900">Job Cleanup</h3>
            <div className="flex items-center space-x-3">
              <div className="flex items-center space-x-2">
                <label className="text-sm text-gray-500">Older than (days):</label>
                <input
                  type="number"
                  min="1"
                  max="365"
                  value={cleanupDays}
                  onChange={(e) => setCleanupDays(parseInt(e.target.value))}
                  className="w-20 px-2 py-1 text-sm border border-gray-300 rounded"
                />
              </div>
              <button
                onClick={() => cleanupOldJobsMutation.mutate()}
                disabled={cleanupOldJobsMutation.isLoading || (cleanupJobs?.data?.length || 0) === 0}
                className="btn btn-danger btn-sm flex items-center"
              >
                <Trash2 className="h-4 w-4 mr-1" />
                Cleanup Old Jobs
              </button>
            </div>
          </div>
        </div>
        <div className="card-body">
          {cleanupLoading ? (
            <div className="flex items-center justify-center h-32">
              <div className="loading-spinner"></div>
            </div>
          ) : cleanupJobs?.data?.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="table">
                <thead>
                  <tr>
                    <th>Job Name</th>
                    <th>Status</th>
                    <th>Completed At</th>
                    <th>Created By</th>
                    <th>Age</th>
                  </tr>
                </thead>
                <tbody>
                  {cleanupJobs.data.map((job) => (
                    <tr key={job.id}>
                      <td className="font-medium">{job.name}</td>
                      <td>
                        <span className={`status-badge status-${job.status.toLowerCase()}`}>
                          {job.status}
                        </span>
                      </td>
                      <td className="text-sm text-gray-500">
                        {job.completedAt ? new Date(job.completedAt).toLocaleString() : '-'}
                      </td>
                      <td className="text-sm text-gray-500">{job.createdBy}</td>
                      <td className="text-sm text-gray-500">
                        {job.completedAt 
                          ? `${Math.floor((new Date() - new Date(job.completedAt)) / 86400000)} days`
                          : '-'
                        }
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-8">
              <Database className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">No jobs to cleanup</h3>
              <p className="mt-1 text-sm text-gray-500">
                No completed or failed jobs older than {cleanupDays} days.
              </p>
            </div>
          )}
        </div>
      </div>

      {/* System Health */}
      <div className="card">
        <div className="card-header">
          <h3 className="text-lg font-medium text-gray-900">System Health</h3>
        </div>
        <div className="card-body">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <Server className="h-8 w-8 text-green-500" />
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-900">Application</p>
                <p className="text-sm text-green-600">Online</p>
              </div>
            </div>
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <Database className="h-8 w-8 text-green-500" />
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-900">Database</p>
                <p className="text-sm text-green-600">Connected</p>
              </div>
            </div>
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <Activity className="h-8 w-8 text-green-500" />
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-900">Scheduler</p>
                <p className="text-sm text-green-600">Active</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Admin;
