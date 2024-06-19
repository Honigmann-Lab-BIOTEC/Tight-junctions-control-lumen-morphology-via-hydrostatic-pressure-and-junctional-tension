clear all;  % Clear all variables from the workspace
close all;  % Close all open figure windows

frame_cut = 3;  % Set the frame cut index to 3

%% Calculate initial recoil
[file, path] = uigetfile('MultiSelect', 'on');  % Open file selection dialog to select multiple files
cd(path);  % Change directory to the selected path
deltaV0 = [];  % Initialize an empty array to store initial velocities
all_dist = [];  % Initialize an empty array to store all distances

for i = 1:size(file, 2)  % Loop through each selected file
    load(file{i});  % Load the current file

    % Crop data to start at frame_cut
    dist_fit = distance(frame_cut:end);  % Extract the distance data from frame_cut to the end
    t_fit = time(frame_cut:end) - time(frame_cut);  % Extract and adjust the time data from frame_cut to the end

    %% Use the first two data points to calculate the initial linear slope
    deltaD = dist_fit(2) - dist_fit(1);  % Calculate the change in distance between the first two data points
    deltaT = t_fit(2) - t_fit(1);  % Calculate the change in time between the first two data points
    deltaV0(i) = deltaD / deltaT;  % Calculate the initial velocity and store it

    figure(1);  % Create or select figure 1
    hold on;  % Hold the current figure to plot multiple datasets
    plot(t_fit, dist_fit, 'o');  % Plot the fitted distance data
    % plot(time, distance, 'o');  % (Optional) plot the original distance data
    all_dist(:, i) = distance;  % Store the original distance data in the all_dist array
end

mean_dist = mean(all_dist, 2);  % Calculate the mean distance across all files
sem_dist = std(all_dist, [], 2) / sqrt(size(all_dist, 2));  % Calculate the standard error of the mean distance

figure(2);  % Create or select figure 2
errorbar(time, mean_dist, sem_dist);  % Plot the mean distance with error bars

time = time';  % Transpose the time array
deltaV0 = deltaV0';  % Transpose the initial velocity array
