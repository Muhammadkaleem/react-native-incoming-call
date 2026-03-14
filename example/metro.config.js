const path = require('path');
const { getDefaultConfig } = require('@react-native/metro-config');

const root = path.resolve(__dirname, '..');
const defaultConfig = getDefaultConfig(__dirname);

/**
 * Metro configuration for monorepo setup.
 * @type {import('metro-config').MetroConfig}
 */
module.exports = {
  ...defaultConfig,

  projectRoot: __dirname,

  // Watch the monorepo root so Metro can resolve the library source
  watchFolders: [root],

  resolver: {
    ...defaultConfig.resolver,
    extraNodeModules: {
      // Prevent duplicate React / React Native from the library's node_modules
      'react': path.join(__dirname, 'node_modules', 'react'),
      'react-native': path.join(__dirname, 'node_modules', 'react-native'),
      'react-native-nitro-modules': path.join(
        __dirname,
        'node_modules',
        'react-native-nitro-modules'
      ),
    },
    // Serve the local library from its source directly to avoid broken
    // relative paths in the compiled lib/ output.
    resolveRequest: (context, moduleName, platform) => {
      if (moduleName === 'react-native-incoming-call') {
        return {
          filePath: path.join(root, 'src/index.tsx'),
          type: 'sourceFile',
        };
      }
      return context.resolveRequest(context, moduleName, platform);
    },
  },
};
