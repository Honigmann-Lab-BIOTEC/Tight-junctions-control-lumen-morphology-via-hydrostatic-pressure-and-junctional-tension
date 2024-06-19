clear all; close all;

% Input variables
channel = 1;      % Channel of the measurement to be analyzed
ups = 5;          % Upsampling factor
RS = 21;          % Size in pixels of the square ROI for vertex detection
max_frame = 307;  % Maximum number of frames to analyze

% Select and load files
[file, pfad] = uigetfile('*.czi', 'MultiSelect', 'on');
cd(pfad);

% Load first stack of data
data = bfopen(file{1});
temp = data{1};

% Initialize storage for image data
stack_all = zeros([size(temp{1, 1}), size(temp, 1)]);
ch = zeros(size(temp, 1), 1);

% Extract image and channel data
for i = 1:size(temp, 1)
    stack_all(:, :, i) = temp{i, 1};
    ind = strfind(temp{i, 2}, 'C=');
    ch(i) = str2double(temp{i, 2}(ind + 2));
    if isnan(ch(i))
        ch(i) = 1;
    end
end

% Get pixel size from metadata
info = data{2};
pxz = info.get('Global Experiment|AcquisitionBlock|AcquisitionModeSetup|ScalingX #1');
pxz = str2double(pxz);
pixsz = pxz * 1e6;  % Convert to micrometers

% Select frames for the specified channel
stack = stack_all(:, :, ch == channel);
n_frames_1 = size(stack, 3);

% Load second stack of data
data = bfopen(file{2});
temp = data{1};
stack_all_2 = zeros([size(temp{1, 1}), size(temp, 1)]);
ch = zeros(size(temp, 1), 1);

% Extract image and channel data
for i = 1:size(temp, 1)
    stack_all_2(:, :, i) = temp{i, 1};
    ind = strfind(temp{i, 2}, 'C=');
    ch(i) = str2double(temp{i, 2}(ind + 2));
    if isnan(ch(i))
        ch(i) = 1;
    end
end

% Select frames for the specified channel
stack_2 = stack_all_2(:, :, ch == channel);
n_frames_2 = size(stack_2, 3);

% Combine both stacks
start_f = n_frames_1 + 1;
end_f = n_frames_1 + n_frames_2;
stack(:, :, start_f:end_f) = stack_2;
n_frames = size(stack, 3);

% Process each frame
for i = 1:n_frames
    im = imresize(stack(:, :, i), ups);
    im = imgaussfilt(im, 3);

    % Initial frame: define ROI
    if i == 1
        ROI_size = [RS, RS] * ups;
        s = size(im);
        center = [s(1)/2, s(2)/2 + 3];  % Center in the middle of the image
        rect = [center(2) - ROI_size(2)/2, center(1) - ROI_size(1)/2, ROI_size(2), ROI_size(1)];
        
        figure(1);
        hold off;
        imagesc(im);
        axis image; axis off;
        colormap gray;

        % Define the first ROI
        h = drawrectangle('Position', rect);
        pos_1 = customWait(h);
        ROI_1 = imcrop(im, pos_1);

        % Define the second ROI
        h = drawrectangle('Position', rect);
        pos_2 = customWait(h);
        ROI_2 = imcrop(im, pos_2);
        dist(i) = pdist([pos_1(1:2); pos_2(1:2)]);
    else
        % Calculate cross-correlation between ROI_1 and the current frame
        c = normxcorr2(ROI_1, im);
        [max_c, imax] = max(abs(c(:)));
        [ypeak, xpeak] = ind2sub(size(c), imax(1));
        corr_offset = [(xpeak - (size(ROI_1, 2) / 2)), (ypeak - (size(ROI_1, 1) / 2))];

        figure(1);
        hold off;
        imagesc(im);
        axis image; axis off;
        colormap gray;
        hold on;
        plot(corr_offset(1), corr_offset(2), 'r+', 'MarkerSize', 15);

        pos_1 = [xpeak - ROI_size(1), ypeak - ROI_size(1), ROI_size(1), ROI_size(2)];
        
        % Calculate cross-correlation between ROI_2 and the current frame
        c = normxcorr2(ROI_2, im);
        [max_c, imax] = max(abs(c(:)));
        [ypeak, xpeak] = ind2sub(size(c), imax(1));
        corr_offset = [(xpeak - (size(ROI_1, 2) / 2)), (ypeak - (size(ROI_1, 1) / 2))];

        plot(corr_offset(1), corr_offset(2), 'r+', 'MarkerSize', 15);

        pos_2 = [xpeak - ROI_size(1), ypeak - ROI_size(1), ROI_size(1), ROI_size(2)];
        
        dist(i) = pdist([pos_1(1:2); pos_2(1:2)]);
        
        ROI_1 = imcrop(im, pos_1);
        ROI_2 = imcrop(im, pos_2);
    end
    drawnow;
end

mean_d0 = mean(dist(1:3));
distance = (dist - mean_d0) * pixsz / ups;

% Extract frame times for the first stack
r = bfGetReader(file{1});
m = r.getMetadataStore();
nPlanes = m.getPlaneCount(0);
time_1 = zeros(1, nPlanes);
for i = 1:nPlanes
    time_1(i) = double(m.getPlaneDeltaT(0, i-1).value());
end
time_1 = time_1 - time_1(1);

% Extract frame times for the second stack
r = bfGetReader(file{2});
m = r.getMetadataStore();
nPlanes = m.getPlaneCount(0);
time_2 = zeros(1, nPlanes);
for i = 1:nPlanes
    time_2(i) = double(m.getPlaneDeltaT(0, i-1).value());
end
time_2 = time_2 - time_2(1);

% Combine times from both stacks
time_2 = time_2 + time_1(end) + 1;
time = [time_1, time_2];

% Plot distance over time
figure;
plot(time, distance, '.-');
xlabel('Time [s]');
ylabel('Distance [um]');

% Fit a model to the distance data (commented out)
% distance_fit = distance(3:end);
% time_fit = time(3:end) - time(3);
% modelfun = @(b,x)((b(1)/b(2))*(1-exp(-b(2)*x)) + b(3)*(1-exp(-b(4)*x)));
% opts = statset('nlinfit');
% beta0 = [1; 2; 1; 10];
% beta = nlinfit(time_fit, distance_fit, modelfun, beta0, opts);
% y_fit = modelfun(beta, time_fit);
% hold on;
% plot(time_fit + time(3), y_fit, 'r-');

% Save the results
fname = file{1};
sname = [fname(1:end-4), '-Recoil.mat'];
save(sname, 'pixsz', 'time', 'distance');
