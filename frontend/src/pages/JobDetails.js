import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { 
  ArrowLeft, 
  Calendar, 
  Clock, 
  User, 
  Play, 
  Square, 
  Trash2,
  RefreshCw,
  AlertCircle,
  CheckCircle,
  XCircle,
  Activity
} from 'lucide-react';
import { jobsAPI } from '../services/api';
import toast from 'react-hot-toast';
import { format } from 'date-fns';

function JobDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState('details');

  const { data: jobData, isLoading } = useQuery(
    ['job', id],
    () => jobsAPI.getJob(id, true),
    { refetchInterval: 5000 } // Refresh every 5 seconds for real-time updates
  );

  const cancelJobMutation = useMutation(
    () => jobsAPI.cancelJob(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['job', id]);
        toast.success('Job cancelled successfully');
      },
      onError: (error) => {
        toast.error(error.response?.data?.message || 'Failed to cancel job');
      },
    }
  );

  const deleteJobMutation = useMutation(
    () => jobsAPI.deleteJob(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('jobs');
        toast.success('Job deleted successfully');
        navigate('/jobs');
      },
      onError: (error) => {
        toast.error(error.response?.data?.message || 'Failed to delete job');
      },
    }
  );

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  const job = jobData?.data;
  if (!job) {
    return (
      <div className="text-center py-12">
        <AlertCircle className="mx-auto h-12 w-12 text-gray-400" />
        <h3 className="mt-2 text-sm font-medium text-gray-900">Job not found</h3>
        <p className="mt-1 text-sm text-gray-500">The job you're looking for doesn't exist.</p>
        <button
          onClick={() => navigate('/jobs')}
          className="mt-4 btn btn-primary"
        >
          Back to Jobs
        </button>
      </div>
    );
  }

  const getStatusIcon = (status) => {
    switch (status) {
      case 'SCHEDULED':
        return <Clock className="h-5 w-5 text-blue-500" />;
      case 'RUNNING':
        return <Play className="h-5 w-5 text-yellow-500" />;
      case 'COMPLETED':
        return <CheckCircle className="h-5 w-5 text-green-500" />;
      case 'FAILED':
        return <XCircle className="h-5 w-5 text-red-500" />;
      case 'CANCELLED':
        return <Square className="h-5 w-5 text-gray-500" />;
      case 'RETRYING':
        return <RefreshCw className="h-5 w-5 text-orange-500" />;
      default:
        return <Clock className="h-5 w-5 text-blue-500" />;
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'SCHEDULED':
        return 'status-scheduled';
      case 'RUNNING':
        return 'status-running';
      case 'COMPLETED':
        return 'status-completed';
      case 'FAILED':
        return 'status-failed';
      case 'CANCELLED':
        return 'status-cancelled';
      case 'RETRYING':
        return 'status-retrying';
      default:
        return 'status-scheduled';
    }
  };

  const handleCancel = () => {
    if (window.confirm('Are you sure you want to cancel this job?')) {
      cancelJobMutation.mutate();
    }
  };

  const handleDelete = () => {
    if (window.confirm('Are you sure you want to delete this job? This action cannot be undone.')) {
      deleteJobMutation.mutate();
    }
  };

  const tabs = [
    { id: 'details', name: 'Details', icon: Calendar },
    { id: 'logs', name: 'Execution Logs', icon: Activity },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <button
            onClick={() => navigate('/jobs')}
            className="p-2 text-gray-400 hover:text-gray-600"
          >
            <ArrowLeft className="h-5 w-5" />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">{job.name}</h1>
            <p className="mt-1 text-sm text-gray-500">{job.description || 'No description'}</p>
          </div>
        </div>
        <div className="flex items-center space-x-3">
          <span className={`status-badge ${getStatusColor(job.status)} flex items-center`}>
            {getStatusIcon(job.status)}
            <span className="ml-1">{job.status}</span>
          </span>
          {(job.status === 'SCHEDULED' || job.status === 'RETRYING') && (
            <button
              onClick={handleCancel}
              disabled={cancelJobMutation.isLoading}
              className="btn btn-warning btn-sm flex items-center"
            >
              <Square className="h-4 w-4 mr-1" />
              Cancel
            </button>
          )}
          <button
            onClick={handleDelete}
            disabled={deleteJobMutation.isLoading}
            className="btn btn-danger btn-sm flex items-center"
          >
            <Trash2 className="h-4 w-4 mr-1" />
            Delete
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex items-center py-2 px-1 border-b-2 font-medium text-sm ${
                  activeTab === tab.id
                    ? 'border-primary-500 text-primary-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                <Icon className="h-4 w-4 mr-2" />
                {tab.name}
              </button>
            );
          })}
        </nav>
      </div>

      {/* Tab Content */}
      {activeTab === 'details' && (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          {/* Job Information */}
          <div className="card">
            <div className="card-header">
              <h3 className="text-lg font-medium text-gray-900">Job Information</h3>
            </div>
            <div className="card-body space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-gray-500">Job ID</label>
                  <p className="text-sm text-gray-900">{job.id}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Job Type</label>
                  <p className="text-sm text-gray-900">{job.jobType}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Priority</label>
                  <p className="text-sm text-gray-900">{job.priority}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Max Retries</label>
                  <p className="text-sm text-gray-900">{job.maxRetries}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Current Retries</label>
                  <p className="text-sm text-gray-900">{job.currentRetryCount}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Created By</label>
                  <p className="text-sm text-gray-900">{job.createdBy}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Timing Information */}
          <div className="card">
            <div className="card-header">
              <h3 className="text-lg font-medium text-gray-900">Timing Information</h3>
            </div>
            <div className="card-body space-y-4">
              <div className="space-y-3">
                <div className="flex items-center">
                  <Calendar className="h-4 w-4 text-gray-400 mr-2" />
                  <div>
                    <label className="text-sm font-medium text-gray-500">Created At</label>
                    <p className="text-sm text-gray-900">
                      {format(new Date(job.createdAt), 'MMM dd, yyyy HH:mm:ss')}
                    </p>
                  </div>
                </div>
                {job.scheduledAt && (
                  <div className="flex items-center">
                    <Clock className="h-4 w-4 text-gray-400 mr-2" />
                    <div>
                      <label className="text-sm font-medium text-gray-500">Scheduled At</label>
                      <p className="text-sm text-gray-900">
                        {format(new Date(job.scheduledAt), 'MMM dd, yyyy HH:mm:ss')}
                      </p>
                    </div>
                  </div>
                )}
                {job.startedAt && (
                  <div className="flex items-center">
                    <Play className="h-4 w-4 text-gray-400 mr-2" />
                    <div>
                      <label className="text-sm font-medium text-gray-500">Started At</label>
                      <p className="text-sm text-gray-900">
                        {format(new Date(job.startedAt), 'MMM dd, yyyy HH:mm:ss')}
                      </p>
                    </div>
                  </div>
                )}
                {job.completedAt && (
                  <div className="flex items-center">
                    <CheckCircle className="h-4 w-4 text-gray-400 mr-2" />
                    <div>
                      <label className="text-sm font-medium text-gray-500">Completed At</label>
                      <p className="text-sm text-gray-900">
                        {format(new Date(job.completedAt), 'MMM dd, yyyy HH:mm:ss')}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Schedule Information */}
          {job.jobSchedule && (
            <div className="card">
              <div className="card-header">
                <h3 className="text-lg font-medium text-gray-900">Schedule Information</h3>
              </div>
              <div className="card-body space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-500">Schedule Type</label>
                    <p className="text-sm text-gray-900">{job.jobSchedule.scheduleType}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">Active</label>
                    <p className="text-sm text-gray-900">
                      {job.jobSchedule.isActive ? 'Yes' : 'No'}
                    </p>
                  </div>
                  {job.jobSchedule.cronExpression && (
                    <div className="col-span-2">
                      <label className="text-sm font-medium text-gray-500">Cron Expression</label>
                      <p className="text-sm text-gray-900 font-mono">
                        {job.jobSchedule.cronExpression}
                      </p>
                    </div>
                  )}
                  {job.jobSchedule.nextExecution && (
                    <div>
                      <label className="text-sm font-medium text-gray-500">Next Execution</label>
                      <p className="text-sm text-gray-900">
                        {format(new Date(job.jobSchedule.nextExecution), 'MMM dd, yyyy HH:mm:ss')}
                      </p>
                    </div>
                  )}
                  {job.jobSchedule.lastExecution && (
                    <div>
                      <label className="text-sm font-medium text-gray-500">Last Execution</label>
                      <p className="text-sm text-gray-900">
                        {format(new Date(job.jobSchedule.lastExecution), 'MMM dd, yyyy HH:mm:ss')}
                      </p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Payload */}
          {job.payload && (
            <div className="card">
              <div className="card-header">
                <h3 className="text-lg font-medium text-gray-900">Job Payload</h3>
              </div>
              <div className="card-body">
                <pre className="bg-gray-50 p-4 rounded-lg text-sm overflow-x-auto">
                  {JSON.stringify(JSON.parse(job.payload), null, 2)}
                </pre>
              </div>
            </div>
          )}

          {/* Error Information */}
          {job.errorMessage && (
            <div className="card">
              <div className="card-header">
                <h3 className="text-lg font-medium text-gray-900">Error Information</h3>
              </div>
              <div className="card-body">
                <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                  <pre className="text-sm text-red-800 whitespace-pre-wrap">
                    {job.errorMessage}
                  </pre>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {activeTab === 'logs' && (
        <JobLogs jobId={id} />
      )}
    </div>
  );
}

function JobLogs({ jobId }) {
  const [page, setPage] = useState(0);
  const { data: logsData, isLoading } = useQuery(
    ['jobLogs', jobId, page],
    () => jobsAPI.getJobLogs(jobId, { page, size: 20 }),
    { refetchInterval: 10000 } // Refresh every 10 seconds
  );

  const logs = logsData?.data?.content || [];
  const totalPages = logsData?.data?.totalPages || 0;

  const getLogLevelColor = (level) => {
    switch (level) {
      case 'ERROR':
        return 'text-red-600 bg-red-50';
      case 'WARN':
        return 'text-yellow-600 bg-yellow-50';
      case 'INFO':
        return 'text-blue-600 bg-blue-50';
      case 'DEBUG':
        return 'text-gray-600 bg-gray-50';
      default:
        return 'text-gray-600 bg-gray-50';
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-32">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="card">
        <div className="card-header">
          <h3 className="text-lg font-medium text-gray-900">Execution Logs</h3>
        </div>
        <div className="card-body p-0">
          {logs.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="table">
                <thead>
                  <tr>
                    <th>Timestamp</th>
                    <th>Level</th>
                    <th>Message</th>
                    <th>Thread</th>
                    <th>Duration</th>
                  </tr>
                </thead>
                <tbody>
                  {logs.map((log) => (
                    <tr key={log.id}>
                      <td className="text-sm text-gray-500">
                        {format(new Date(log.createdAt), 'MMM dd, HH:mm:ss.SSS')}
                      </td>
                      <td>
                        <span className={`status-badge ${getLogLevelColor(log.logLevel)}`}>
                          {log.logLevel}
                        </span>
                      </td>
                      <td>
                        <div className="text-sm text-gray-900">{log.message}</div>
                        {log.details && (
                          <div className="text-xs text-gray-500 mt-1">{log.details}</div>
                        )}
                      </td>
                      <td className="text-sm text-gray-500">{log.threadName || '-'}</td>
                      <td className="text-sm text-gray-500">
                        {log.durationMs ? `${log.durationMs}ms` : '-'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-8">
              <Activity className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">No logs available</h3>
              <p className="mt-1 text-sm text-gray-500">
                Execution logs will appear here when the job runs.
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between">
          <div className="text-sm text-gray-700">
            Showing page {page + 1} of {totalPages}
          </div>
          <div className="flex space-x-2">
            <button
              onClick={() => setPage(page - 1)}
              disabled={page === 0}
              className="btn btn-secondary btn-sm disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Previous
            </button>
            <button
              onClick={() => setPage(page + 1)}
              disabled={page >= totalPages - 1}
              className="btn btn-secondary btn-sm disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Next
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default JobDetails;
