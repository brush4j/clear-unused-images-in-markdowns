package org.lyflexi.clearunusedimagesinmarkdowns;

/**
 * @Description:
 * @Author: lyflexi
 * @project: clear-unused-images-in-markdowns
 * @Date: 2024/9/9 19:24
 */
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

@Slf4j
public class UnusedImagesFinder {
    // 匹配 Markdown 中的图片引用 ![alt](path) 的正则表达式
    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[.*?\\]\\((.*?)\\)");
    // 设置扫描根路径
    private static final String DIRECTORY_PATH = "E:\\github\\vsNotes";
    // 排除附件目录
    private static final String EXCLUDE_IMAGES_DIR = "E:\\github\\vsNotes\\appendix-drawio";
    // 回收站目录
    private static final String UNUSED_IMAGES_DIR = "E:\\github\\vsNotes\\unused-images";

    public static void main(String[] args) throws IOException {

        //初始化的时候创建回收站
        mkdirUnusedImagesDirIfNotExist(UNUSED_IMAGES_DIR);
        
        // 获取所有 Markdown 文件
        List<File> markdownFiles = getFilesWithExtension(DIRECTORY_PATH, ".md");

        // 获取所有图片文件(使用的是绝对路径)
        List<File> imageFiles = getFilesWithExtensions(DIRECTORY_PATH, Arrays.asList(".png", ".jpg", ".jpeg", ".gif", ".bmp", ".svg"));

        // 获取已经存在于 UNUSED_IMAGES_DIR 文件夹中的图片(使用的是绝对路径)
        List<File> unusedImages = getFilesWithExtensions(UNUSED_IMAGES_DIR, Arrays.asList(".png", ".jpg", ".jpeg", ".gif", ".bmp", ".svg"));

        // 获取已经存在于 EXCLUDE_IMAGES_DIR 文件夹中的图片(使用的是绝对路径)
        List<File> excludeImages = getFilesWithExtensions(EXCLUDE_IMAGES_DIR, Arrays.asList(".png", ".jpg", ".jpeg", ".gif", ".bmp", ".svg"));

        //排除附件目录中的图片
        imageFiles.removeAll(excludeImages);
        
        //回收站中的图片不做二次移动
        imageFiles.removeAll(unusedImages);

        // 从 Markdown 文件中提取所有引用的图片名称！注意是名称，不是相对路径，也不是绝对路径
        Set<String> referencedImages = extractReferencedImages(markdownFiles);

        // 找出未被引用且不在 UNUSED_IMAGES_DIR 文件夹中的图片(使用图片名称过滤)
        List<File> unreferencedImages = calculateUnreferencedImages(imageFiles, referencedImages);

        // 将未被引用的图片移动到 UNUSED_IMAGES_DIR 文件夹
        moveFilesToDirectory(unreferencedImages, UNUSED_IMAGES_DIR);
    }

    /**
     * 计算未被引用的图片
     * @param imageFiles
     * @param referencedImages
     * @return 未被引用的图片名称集合
     */
    private static List<File> calculateUnreferencedImages(List<File> imageFiles, Set<String> referencedImages) {
        List<File> unreferencedImages = imageFiles.stream()
                .filter(image -> {
                    String imageSuffix = getImageSuffixByAbsolutePath(image.getPath());
                    return !referencedImages.contains(imageSuffix);
                })
                .collect(Collectors.toList());
        return unreferencedImages;
    }

    /**
     * 从所有 Markdown 文件中提取引用的图片的名称
     * @param absolutePath
     * @return
     */
    private static String getImageSuffixByAbsolutePath(String absolutePath) {
        // 找到最后一个斜杠的位置
        int lastIndex = absolutePath.lastIndexOf('\\');
        // 从该位置的下一个字符开始截取，返回文件名
        return absolutePath.substring(lastIndex + 1);
    }

    /**
     * 创建目录
     * @param unusedImagesDir
     */
    private static void mkdirUnusedImagesDirIfNotExist(String unusedImagesDir) {
        File dir = new File(unusedImagesDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 获取指定目录下的所有具有特定扩展名的文件
     * @param directoryPath
     * @param extension
     * @return
     * @throws IOException
     */
    private static List<File> getFilesWithExtension(String directoryPath, String extension) throws IOException {
        return Files.walk(Paths.get(directoryPath))
                .filter(path -> path.toString().endsWith(extension))
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定目录下的所有具有多个扩展名的文件
     * @param directoryPath
     * @param extensions
     * @return
     * @throws IOException
     */
    private static List<File> getFilesWithExtensions(String directoryPath, List<String> extensions) throws IOException {
        return Files.walk(Paths.get(directoryPath))
                .filter(path -> {
                    String filePath = path.toString().toLowerCase();
                    return extensions.stream().anyMatch(filePath::endsWith);
                })
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    /**
     * 从所有 Markdown 文件中提取引用的图片的名称即可
     * eg.我有图片路径为.image/test/20240801.png，20240801.png
     * @param markdownFiles
     * @return
     * @throws IOException
     */
    private static Set<String> extractReferencedImages(List<File> markdownFiles) throws IOException {
        Set<String> referencedImages = new HashSet<>();
        for (File markdownFile : markdownFiles) {
            List<String> lines = Files.readAllLines(markdownFile.toPath());
            for (String line : lines) {
                Matcher matcher = IMAGE_PATTERN.matcher(line);
                while (matcher.find()) {
                    //imagePath =  image/test/20240801.png  这是markdown中的路径写法：正斜杠/
                    String imagePath = matcher.group(1);
                    //absolutePath =  image\test\20240801.png   这是绝对路径中的写法：反斜杠\
                    String absolutePath = new File(imagePath).getPath();
                    referencedImages.add(getImageSuffixByAbsolutePath(absolutePath));
                }
            }
        }
        return referencedImages;
    }

    /**
     * 将文件移动到指定目录
     * @param files
     * @param destinationDir
     * @throws IOException
     */
    private static void moveFilesToDirectory(List<File> files, String destinationDir) throws IOException {
        File dir = new File(destinationDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if(files.isEmpty()) {
            log.info("未找到无用的图片");
            return;
        }
        for (File file : files) {
            Path destinationPath = Paths.get(destinationDir, file.getName());
            Files.move(file.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("图片名称：{}，图片原始路径：{}，已经被移动到指定目录：{}", file.getName(), file.getAbsolutePath(),destinationPath);
        }
        log.info("所有未被引用的图片已移动到{},总移动数量：{}", destinationDir,files.size());
        return;
    }
}
